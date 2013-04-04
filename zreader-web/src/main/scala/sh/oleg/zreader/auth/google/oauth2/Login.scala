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

package sh.oleg.zreader.auth.google.oauth2

import scala.Enumeration
import net.liftweb.util.Helpers._

package object Login {

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

  /** Determines if the Google Authorization Server returns an authorization code, or an opaque access token. */
  object ResponseType extends Enumeration {
    /** if your application:
     *   - requires long-running access to user data through a Google API without the user being present
     *   - is primarily using server side or installed application components
     */
    val code = Value
    /** if you're writing a JavaScript-heavy application */
    val token = Value
  }
  type ResponseType = ResponseType.Value

  /** Indicates if the user should be re-prompted for consent. */
  object ApprovalPrompt extends Enumeration {
    /** given user should only see the consent page for a given set of scopes the first time through the sequence. */
    val auto = Value
    /** user sees a consent page even if they have previously given consent to your application for a given set of scopes. */
    val force = Value
  }
  type ApprovalPrompt = ApprovalPrompt.Value

  /** Indicates if your application needs to access a Google API when the user is not present at the browser. */
  object AccessType extends Enumeration {
    val online = Value
    /**
     * Use `offline` if your application needs to refresh access tokens when the user is not present at the browser
     * This will result in your application obtaining a refresh token the first time your application exchanges an authorization code for a user.
     * */
    val offline = Value
  }
  type AccessType = AccessType.Value

  /**
   * Settings for use Google OAuth2 (see [[https://developers.google.com/accounts/docs/OAuth2WebServer Using OAuth 2.0 for Web Server Applications]] for details
   *
   * @param clientId Indicates the client that is making the request.
   *                 The value must exactly match the value shown in the [[https://code.google.com/apis/console#access APIs Console]]
   * @param redirectUri one of your redirect_uris registered at the [[https://code.google.com/apis/console#access APIs Console]]
   *                    Determines where the response is sent.
   *                    The value of this parameter must exactly match one of the values registered in the [[https://code.google.com/apis/console#access APIs Console]]
   *                    (including the http or https schemes, case, and trailing '/').
   * @param scope Indicates the Google API access your application is requesting.
   *              The values passed in this parameter inform the consent page shown to the user.
   *              There is an inverse relationship between the number of permissions requested
   *              and the likelihood of obtaining user consent.
   * @param state This optional parameter indicates any state which may be useful to your application upon receipt of the response.
   *              The Google Authorization Server roundtrips this parameter, so your application receives the same value it sent.
   *              Possible uses include redirecting the user to the correct resource in your site, nonces,
   *              and cross-site-request-forgery mitigations.
   * @param responseType see [[sh.oleg.zreader.auth.google.oauth2.Login.ResponseType]]
   * @param accessType see [[sh.oleg.zreader.auth.google.oauth2.Login.AccessType]]
   * @param approvalPrompt see [[sh.oleg.zreader.auth.google.oauth2.Login.ApprovalPrompt]]
   */
  case class Settings(clientId: String,
                      redirectUri: String,
                      scope: Set[UserInfo] = Set(UserInfo.email, UserInfo.profile),
                      state: Option[String] = None,
                      responseType: Option[ResponseType] = None,
                      accessType: Option[AccessType] = None,
                      approvalPrompt: Option[ApprovalPrompt] = None) {
    def addUserInfo(ui: UserInfo) = this.copy(scope = this.scope + ui)
    def delUserIfo(ui: UserInfo) = this.copy(scope = this.scope - ui)
    def withState(state: String) = this.copy(state = Some(state))
    def withResponseType(value: ResponseType) = this.copy(responseType = Some(value))
    def withAccessType(value: AccessType) = this.copy(accessType = Some(value))
    def withApprovalPrompt(value: ApprovalPrompt) = this.copy(approvalPrompt = Some(value))
  }

  /**
   * The [[http://liftweb.net/ Lift]] [[http://stable.simply.liftweb.net/#toc-Section-7.1 Snippet]] for [[https://developers.google.com/accounts/docs/OAuth2WebServer#formingtheurl Forming the URL]]
   *
   * @param settings see [[sh.oleg.zreader.auth.google.oauth2.Login.Settings]]
   */
  case class Snippet(settings: Settings) {
    def render = {
      def always(name: String)(value: String) =
        Some((name, value))
      def option(name: String)(value: Option[String]) =
        value.map { case v => (name, v) }
      def enum[T <: Enumeration](name: String)(value: Option[T#Value]) =
        value.map { case v => (name, v.toString) }
      def string(name: String)(s: String) =
        if (s.isEmpty) None else Some(name, s)
      "a [href]" #> appendParams("https://accounts.google.com/o/oauth2/auth",
        List(
          string("scope")(settings.scope.map("https://www.googleapis.com/auth/userinfo." + _.toString).mkString(" ")),
          option("state")(settings.state),
          always("redirect_uri")(settings.redirectUri),
          enum("response_type")(settings.responseType),
          always("client_id")(settings.clientId),
          enum("access_type")(settings.accessType),
          enum("approval_prompt")(settings.approvalPrompt)
        ).flatten)
    }
  }
}