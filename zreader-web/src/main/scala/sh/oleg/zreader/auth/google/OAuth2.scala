/*
 * This file is part of Z-Reader (c) 2013
 *
 * Z-Reader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Z-Reader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Z-Reader.  If not, see <http://www.gnu.org/licenses/>.
 */

package sh.oleg.zreader.auth.google

import scala.{throws, Enumeration}
import net.liftweb.http.rest.RestHelper
import net.liftweb.util.Helpers._
import net.liftweb.common.{Box, Failure, Empty, Full}
import net.liftweb.http._
import net.liftweb.http.rest._
import net.liftweb.json.parse
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.{HttpEntity, HttpResponse, NameValuePair}
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import grizzled.slf4j.Logging
import sh.oleg.zreader.auth.google.OAuth2.TokenExchangeProblem.UnExpectedTokenType
import java.io.IOException

package object OAuth2 {
  val entryURL = "https://accounts.google.com/o/oauth2/auth"

  /** Scopes for User Profile Permission */
  object UserInfo extends Enumeration {
    /** Gain read-only access to the user's email address. */
    val email = Value
    /** Gain read-only access to basic profile information, including a
     *   - user identifier
     *   - name
     *   - profile
     *   - photo
     *   - profile URL
     *   - country
     *   - language
     *   - timezone
     *   - birthdate
     */
    val profile = Value
  }
  type UserInfo = UserInfo.Value

  /** Determines if the login Authorization Server returns an authorization code, or an opaque access token. */
  object ResponseType extends Enumeration {
    /** if your application:
     *   - requires long-running access to user data through a login API without the user being present
     *   - is primarily using server side or installed application components
     */
    val code = Value
    /** if you're writing a JavaScript-heavy application */
    val token = Value
  }

  /** Indicates if the user should be re-prompted for consent. */
  object ApprovalPrompt extends Enumeration {
    /** given user should only see the consent page for a given set of scopes the first time through the sequence. */
    val auto = Value
    /** user sees a consent page even if they have previously given consent to your application for a given set of scopes. */
    val force = Value
  }

  /** Indicates if your application needs to access a login API when the user is not present at the browser. */
  object AccessType extends Enumeration {
    val online = Value
    /**
     * Use `offline` if your application needs to refresh access tokens when the user is not present at the browser
     * This will result in your application obtaining a refresh token the first time your application exchanges an authorization code for a user.
     * */
    val offline = Value
  }

  /** Indicates the type of token returned. At this time, this field will always have the value Bearer */
  object TokenType extends Enumeration {
    val Bearer = Value
  }

  /**
   * Token for access to Google API
   * @param accessToken the token that can be sent to a Google API
   * @param refreshToken a token that may be used to obtain a new access token. Refresh tokens are valid until the user revokes access. This field is only present if access_type=offline is included in the authorization code request.
   * @param expiresIn the remaining lifetime on the access token
   * @param tokenType indicates the type of token returned. At this time, this field will always have the value Bearer
   */
  case class Token(accessToken  : String,
                   refreshToken : Option[String]  = None,
                   expiresIn    : Integer,
                   tokenType    : TokenType.Value = TokenType.Bearer)

  object TokenExchangeProblem {
    /**
     * Problem with exchange authorization code to [[sh.oleg.zreader.auth.google.OAuth2.Token]] by Google API
     *
     * @param paramList parameter list used for POST request to [[sh.oleg.zreader.auth.google.OAuth2.entryURL]]
     * @param message problem description
     */
    class Base(paramList : List[(String, String)])(message : String) extends RuntimeException(String.format(
      "Problem with exchange autorisation code to token by Google API: %s. Request URL: %s parameters: %s",
      message, entryURL, paramList.map(p => String.format("%s=%s", p._1, p._2)).mkString(", ")))

    /**
     * Google response is absent (== null)
     *
     * @param paramList see [[sh.oleg.zreader.auth.google.OAuth2.TokenExchangeProblem.Base]]
     */
    class ResponseIsAbsent(paramList : List[(String, String)]) extends Base(paramList)("response is absent")

    /**
     * Google responses is missed (== "")
     *
     * @param paramList see [[sh.oleg.zreader.auth.google.OAuth2.TokenExchangeProblem.Base]]
     */
    class ResponseIsEmpty(paramList : List[(String, String)]) extends Base(paramList)("response is empty")

    /**
     * Unexpected token type for [[sh.oleg.zreader.auth.google.OAuth2.Token]], probably Google extends API
     * @param paramList see [[sh.oleg.zreader.auth.google.OAuth2.TokenExchangeProblem.Base]]
     * @param tokenType actual token type
     */
    class UnExpectedTokenType(paramList : List[(String, String)])(tokenType : String) extends Base(paramList)(
      s"unexpected type of Token for access to Google API: $tokenType (expected: Bearer)")
  }



  /**
   * WebServer for use login OAuth2 (see [[https://developers.google.com/accounts/docs/OAuth2WebServer Using OAuth 2.0 for Web Server Applications]] for details.
   * Please note about https, hostname and path parameters - (https + hostname + path) = one of your redirect_uris registered at the [[https://code.google.com/apis/console#access APIs Console]]
   * Determines where the response is sent.
   * The (https + hostname + path) of this parameter must exactly match one of the values registered in the [[https://code.google.com/apis/console#access APIs Console]]
   * (including the http or https schemes, case, and trailing '/')
   *
   * @param clientId Indicates the client that is making the request.
   *                 The value must exactly match the value shown in the [[https://code.google.com/apis/console#access APIs Console]]
   * @param clientSecret The client secret obtained during application registration
   *                     The value must exactly match the value shown in the [[https://code.google.com/apis/console#access APIs Console]]
   * @param secure when true use https:// for redirectUri, otherwise use http://
   * @param hostname hostname of service. By default would used Lift hostname
   * @param path path for processing google redirect.
   * @param scope Indicates the login API access your application is requesting.
   *              The values passed in this parameter inform the consent page shown to the user.
   *              There is an inverse relationship between the number of permissions requested
   *              and the likelihood of obtaining user consent.
   * @param responseType see [[sh.oleg.zreader.auth.google.OAuth2.ResponseType]]
   * @param accessType see [[sh.oleg.zreader.auth.google.OAuth2.AccessType]]
   * @param approvalPrompt see [[sh.oleg.zreader.auth.google.OAuth2.ApprovalPrompt]]
   */
  case class WebServer(clientId:       String,
                       clientSecret:   String,
                       secure:         Boolean                      = false,
                       hostname:       Option[String]               = None,
                       path:           List[String],
                       scope:          Set[UserInfo]                = Set(UserInfo.email, UserInfo.profile),
                       responseType:   Option[ResponseType.Value]   = None,
                       accessType:     Option[AccessType.Value]     = None,
                       approvalPrompt: Option[ApprovalPrompt.Value] = None) {
    def withClientId      (value : String)              = this.copy(clientId       = value)
    def withClientSecret  (value : String)              = this.copy(clientSecret   = value)
    def withSecure        (value : Boolean)             = this.copy(secure         = value)
    def withHostname      (value : String)              = this.copy(hostname       = Some(value))
    def withPath          (value : List[String])        = this.copy(path           = value)
    def addUserInfo       (ui: UserInfo)                = this.copy(scope          = this.scope + ui)
    def delUserIfo        (ui: UserInfo)                = this.copy(scope          = this.scope - ui)
    def withResponseType  (value: ResponseType.Value)   = this.copy(responseType   = Some(value))
    def withAccessType    (value: AccessType.Value)     = this.copy(accessType     = Some(value))
    def withApprovalPrompt(value: ApprovalPrompt.Value) = this.copy(approvalPrompt = Some(value))
    def redirectUri : String = {
      val path = this.path.mkString("/")
      this.hostname match {
        case Some(hostname) => {
          val scheme = (if (secure) "https" else "http")
          s"$scheme://$hostname/$path"
        }
        case None => s"/$path"
      }
    }

  }

  /** [[https://developers.google.com/accounts/docs/OAuth2WebServer#formingtheurl URL for google OAuth2]]
    * @param settings see [[sh.oleg.zreader.auth.google.OAuth2.WebServer]]
    * @param state This optional parameter indicates any state which may be useful to your application upon receipt of the response.
    *              The login Authorization Server roundtrips this parameter, so your application receives the same value it sent.
    *              Possible uses include redirecting the user to the correct resource in your site, nonces,
    *              and cross-site-request-forgery mitigations.
    */
  def singUpURL(settings: WebServer)(state : Option[String] = None) = {
    def always(name: String)(value: String) =
      Some((name, value))
    def option(name: String)(value: Option[String]) =
      value.map { case v => (name, v) }
    def enum[T <: Enumeration](name: String)(value: Option[T#Value]) =
      value.map { case v => (name, v.toString) }
    def string(name: String)(s: String) =
      if (s.isEmpty) None else Some(name, s)
    import settings._
    appendParams(entryURL,
      List(
        string("scope")          (scope.map("https://www.googleapis.com/auth/userinfo." + _.toString).mkString(" ")),
        option("state")          (state),
        always("redirect_uri")   (redirectUri),
        enum  ("response_type")  (responseType),
        always("client_id")      (clientId),
        enum  ("access_type")    (accessType),
        enum  ("approval_prompt")(approvalPrompt)
      ).flatten)
  }

  case class Callback(settings: WebServer) extends RestHelper with Logging {
    @throws[TokenExchangeProblem.Base]
    @throws[java.io.IOException]
    @throws[org.apache.http.client.ClientProtocolException]
    private def exchange(code: String) = {
      val client = new DefaultHttpClient
      val request = new HttpPost(entryURL)
      val paramList = List(
        ("code",          code),
        ("client_id",     settings.clientId),
        ("client_secret", settings.clientSecret),
        ("redirectUri",   settings.redirectUri),
        ("grantType",     "authorization_code"))
      request.setEntity(new UrlEncodedFormEntity(paramList.map(p => new BasicNameValuePair(p._1, p._2)).asInstanceOf[java.util.List[NameValuePair]]))
      case class TokenParser(access_token : String,
                             refresh_token : Option[String],
                             expires_in : Integer,
                             token_type : String) {
        def toToken : Option[Token] = token_type match {
          case "Bearer" => Some(Token(
            accessToken = access_token,
            refreshToken = refresh_token,
            expiresIn = expires_in))
          case _ => None //unexpected : String => throw new TokenExchangeProblem.UnExpectedTokenType(paramList)(unexpected)
        }
      }
      client.execute(request).getEntity match {
        case null => throw new TokenExchangeProblem.ResponseIsAbsent(paramList)
        case entity => {
          val stream = entity.getContent
          val content = io.Source.fromInputStream(stream).getLines.mkString
          stream.close
          if ("" == content) throw new TokenExchangeProblem.ResponseIsEmpty(paramList)
          parse(content).extract[TokenParser]
        }
      }
    }
    def signup(state : String, code : String)  : Box[LiftResponse] = {
      /*try {
        exchange(code)
      } catch {
      }*/
      Full(RedirectResponse(appendParams("http://localhost:8080/signup", List(("state", state), ("code", code)))))
    }
    def failed(state : String, error : String) : Box[LiftResponse]= {
      Full(RedirectResponse(appendParams("http://localhost:8080/failed", List(("state", state), ("error", error)))))
    }
    def bad(request : Req)                     : Box[LiftResponse] = {
      throw new NotImplementedException
    }

    serve {
      case request @ Req(settings.path, _, GetRequest) => (S.param("state"), S.param("code"), S.param("error")) match {
        case (Full(state), Full(code), _)  => signup(state, code)
        case (Full(state), _, Full(error)) => failed(state, error)
        case _                             => bad(request)

      }
    }
  }
}