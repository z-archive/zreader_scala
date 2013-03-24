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
import net.liftweb._
import sitemap.Loc.Snippet
import util._
import Helpers._
import scala.Enumeration

package object Login {

  /**
   *  scope - space delimited set of permissions the application requests
   *
   *  Indicates the Google API access your application is requesting.
   *  The values passed in this parameter inform the consent page shown to the user.
   *  There is an inverse relationship between the number of permissions requested
   *  and the likelihood of obtaining user consent.
   *
   *  Every item from list would be translated to
   *  https://www.googleapis.com/auth/item
   *
   */
  object UserInfo extends Enumeration {
    Value
    val email, profile = Value
  }
  import UserInfo._
  type UserInfo = UserInfo.Value

  object ResponseType extends Enumeration {
    val code, token = Value
  }
  import ResponseType._
  type ResponseType = ResponseType.Value

  object ApprovalPrompt extends Enumeration {
    val auto, force = Value
  }
  import ApprovalPrompt._
  type ApprovalPrompt = ApprovalPrompt.Value

  object AccessType extends Enumeration {
    val online, offline = Value
  }
  import AccessType._
  type AccessType = AccessType.Value

  case class Snippet(val clientId :       String,
                     val redirectUri :    String,
                     val scope :          Set[UserInfo]         = Set(UserInfo.email, UserInfo.profile),
                     val state :          Option[String]         = None,
                     val responseType :   Option[ResponseType]   = None,
                     val accessType :     Option[AccessType]     = None,
                     val approvalPrompt : Option[ApprovalPrompt] = None) {
    def render = {
      def always(name : String)(value : String) =
        Some((name, value))
      def option(name : String)(value : Option[String]) =
        value.map{case v => (name, v)}
      def enum[T <: Enumeration](name : String)(value : Option[T#Value]) =
        value.map{case v => (name, v.toString)}
      def string(name : String)(s : String) =
        if (s.isEmpty) None else Some(name, s)
      "a [href]" #> appendParams("https://accounts.google.com/o/oauth2/auth",
        List(
          string("scope")          (scope.map("https://www.googleapis.com/auth/userinfo." + _.toString).mkString(" ")),
          option("state")          (state),
          always("redirect_uri")   (redirectUri),
          enum("response_type")    (responseType),
          always("client_id")      (clientId),
          enum("access_type")      (accessType),
          enum("approval_prompt")  (approvalPrompt)
        ).flatten)
    }
    def addScope(ui : UserInfo.Value) = this.copy(scope=this.scope+ui)
    def delScope(ui : UserInfo.Value) = this.copy(scope=this.scope-ui)
    def withState(s :String) =                         this.copy(state=Some(s))
    def withResponseType(e : ResponseType.Value) =     this.copy(responseType=Some(e))
    def withAccessType(e : AccessType.Value) =         this.copy(accessType=Some(e))
    def withApprovalPrompt(e : ApprovalPrompt.Value) = this.copy(approvalPrompt=Some(e))
  }

  /**
   * state - any string
   *
   * This optional parameter indicates any state which may be useful to your application upon receipt of the response.
   * The Google Authorization Server roundtrips this parameter, so your application receives the same value it sent.
   * Possible uses include redirecting the user to the correct resource in your site, nonces,
   * and cross-site-request-forgery mitigations.
   */

  /**
   * redirect_uri - one of your redirect_uris registered at the APIs Console at https://code.google.com/apis/console#access
   *
   * Determines where the response is sent.
   * The value of this parameter must exactly match one of the values registered in the APIs Console
   * (including the http or https schemes, case, and trailing '/').
   */

  /**
   * response_type - code or token
   *
   * Determines if the Google Authorization Server returns an authorization code, or an opaque access token.
   */
  /**
   * client_id - the client_id obtained from the APIs Console
   *
   * Indicates the client that is making the request.
   * The value passed in this parameter must exactly match the value shown in the APIs Console at https://code.google.com/apis/console#access
   */

  /**
   * access_type - online or offline
   *
   * Indicates if your application needs to access a Google API when the user is not present at the browser.
   * This parameter defaults to online.
   * If your application needs to refresh access tokens when the user is not present at the browser, then use offline.
   * This will result in your application obtaining a refresh token the first time your application exchanges an authorization code for a user.
   */
  /**
   * approval_prompt - force or auto
   *
   * Indicates if the user should be re-prompted for consent.
   * The default is auto, so a given user should only see the consent page for a given set of scopes the first time through the sequence.
   * If the value is force, then the user sees a consent page even if they have previously given consent to your application for a given set of scopes.
   *
   */
}