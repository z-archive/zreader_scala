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

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import sh.oleg.zreader.rss.EntityModule.Feed

@RunWith(classOf[JUnitRunner])
class RSSReaderTest extends Specification {

  trait inReader extends Scope {

    lazy val reader = new RSSReader {}

    def commonValidate(feed: Feed) {
      feed must not beNull;
      feed.items must not beNull;
      feed.items.foreach {
        item =>
          item.content must not beEmpty;
          item.published must beGreaterThan(0l)
          item.permLink must not beEmpty
      }
      feed.items.zip(feed.items.tail).foreach {
        case (l, r) => l.published must be greaterThanOrEqualTo r.published
      }
      feed.items.size must be equalTo (15)
    }

  }

  "RSS reader" should {

    "parse sample RSS feed" in new inReader {
      commonValidate {
        reader.parseFeed(classOf[RSSReaderTest].getResourceAsStream("/rss/java-dzone.xml"))
      }
    }

    "read and parse RSS feed from website" in new inReader {
      val feed = reader.loadFeed("http://feeds.dzone.com/javalobby/frontpage")
      feed must beSome
      commonValidate(feed.get)
    }
  }

}
