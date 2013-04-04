package sh.oleg.zreader.auth

package object Settings {
  import sh.oleg.zreader.auth.google.OAuth2
  import OAuth2.AccessType._
  import OAuth2.ResponseType._
  import OAuth2.ApprovalPrompt._
  import OAuth2.Settings

  val google = Settings(
    clientId="855033541488.apps.googleusercontent.com",
    redirectUri="http://localhost:8080/google/oauth2").
    withState("/profile").
    withAccessType(offline).
    withResponseType(code).
    withApprovalPrompt(force)

}
