package sh.oleg.zreader.auth

package object Settings {
  import sh.oleg.zreader.auth.google.OAuth2
  import OAuth2.AccessType._
  import OAuth2.ResponseType._
  import OAuth2.ApprovalPrompt._
  import OAuth2.WebServer

  val google = WebServer(
    clientId="855033541488.apps.googleusercontent.com",
    clientSecret="9cDpGxfkmZEGNofph75cJb8j",
    path=List("google", "oauth2")).
    withHostname("localhost:8080").
    withAccessType(offline).
    withResponseType(code).
    withApprovalPrompt(force)
    //withState("/profile").

}
