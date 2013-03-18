package sh.oleg.zreader.rss

import sh.oleg.zreader.rss.AuthModule.{BasicHttpAuth, Credentials}
import sh.oleg.zreader.rss.EntityModule.{FeedItem, Feed}
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.{BasicCredentialsProvider, DefaultHttpClient}
import org.apache.http.client.HttpClient
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.client.protocol.ClientContext
import org.apache.http.auth.{UsernamePasswordCredentials, AuthScope}
import org.apache.http.HttpStatus
import java.io.InputStream
import com.sun.syndication.io.{XmlReader, SyndFeedInput}
import com.sun.syndication.feed.synd.SyndEntry

/**
 * User: Eugene Dzhurinsky
 * Date: 3/18/13
 */
trait RSSReader {

  private val poolingConnMgr = new PoolingClientConnectionManager()

  private val httpClient: HttpClient = new DefaultHttpClient(poolingConnMgr)

  type Filter = FeedItem => Boolean

  def loadFeed(url: String,
               creds: Option[_ <: Credentials] = None,
               filter: Filter = _ => true): Option[Feed] = {
    val req = new HttpGet(url)
    val ctx = new BasicHttpContext()
    creds foreach {
      case BasicHttpAuth(uname, pwd) =>
        val credProvider = new BasicCredentialsProvider
        credProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(uname, pwd))
        ctx.setAttribute(ClientContext.CREDS_PROVIDER, credProvider)
    }

    val response = httpClient.execute(req, ctx)
    lazy val content = response.getEntity
    (response.getStatusLine.getStatusCode match {
      case HttpStatus.SC_OK => Some(content.getContent)
      case _ => None
    }) map parseFeed
  }

  def parseFeed(is: InputStream): Feed = {
    val reader = new XmlReader(is)
    val syncInput = new SyndFeedInput().build(reader)
    import collection.JavaConversions._
    new Feed(
      syncInput.getEntries.map {
        case entry: SyndEntry => new FeedItem(entry.getDescription.getValue,
          entry.getLink,
          entry.getPublishedDate.getTime)
      }
    )
  }

}