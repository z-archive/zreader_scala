/**
 *     ZReader - WEB-based RSS reader
 *     Copyright (C) 2013 Oleg Tsarev, Eugene "jdevelop" Dzhurinsky
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Also add information on how to contact you by electronic and paper mail.
 *
 *   If your software can interact with users remotely through a computer
 * network, you should also make sure that it provides a way for users to
 * get its source.  For example, if your program is a web application, its
 * interface could display a "Source" link that leads users to an archive
 * of the code.  There are many ways you could offer source, and different
 * solutions will be better for different programs; see section 13 for the
 * specific requirements.
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU AGPL, see
 * <http://www.gnu.org/licenses/>.
 */
package sh.oleg.zreader.rss

import sh.oleg.zreader.rss.AuthModule.{BasicHttpAuth, Credentials}
import sh.oleg.zreader.rss.EntityModule.{FeedItem, Feed}
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.client.methods.{HttpRequestBase, HttpGet}
import org.apache.http.impl.client.{BasicCredentialsProvider, DefaultHttpClient}
import org.apache.http.client.HttpClient
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.client.protocol.ClientContext
import org.apache.http.auth.{UsernamePasswordCredentials, AuthScope}
import org.apache.http.HttpStatus
import java.io.InputStream
import com.sun.syndication.io.{XmlReader, SyndFeedInput}
import com.sun.syndication.feed.synd.SyndEntry
import sh.oleg.zreader.utils.safeClose

trait RSSReader {

  private val poolingConnMgr = new PoolingClientConnectionManager()

  private val httpClient: HttpClient = new DefaultHttpClient(poolingConnMgr)

  type Filter = FeedItem => Boolean

  def loadFeed(url: String,
               creds: Option[_ <: Credentials] = None,
               filter: Filter = _ => true): Option[Feed] = {
    var req: HttpRequestBase = null
    try {
      req = new HttpGet(url)
      val ctx = new BasicHttpContext()
      creds foreach {
        case BasicHttpAuth(uname, pwd) =>
          val credProvider = new BasicCredentialsProvider
          credProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(uname, pwd))
          ctx.setAttribute(ClientContext.CREDS_PROVIDER, credProvider)
      }

      val response = httpClient.execute(req, ctx)
      lazy val content = response.getEntity
      lazy val stream = content.getContent
      try {
        (response.getStatusLine.getStatusCode match {
          case HttpStatus.SC_OK => Some(stream)
          case _ => None
        }) map parseFeed
      } finally {
        safeClose(stream)
      }
    } finally {
      if (req != null)
        req.releaseConnection()
    }
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