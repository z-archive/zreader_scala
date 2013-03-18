package sh.oleg.zreader.rss

/**
 * User: Eugene Dzhurinsky
 * Date: 3/18/13
 */
object AuthModule {

  sealed class Credentials

  case class BasicHttpAuth(username: String, password: String) extends Credentials

}
