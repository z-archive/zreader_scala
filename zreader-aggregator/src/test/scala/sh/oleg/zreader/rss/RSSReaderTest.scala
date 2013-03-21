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
