package sh.oleg.zreader.rss

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.specification.Scope
import sh.oleg.zreader.rss.EntityModule.Feed

/**
 * User: Eugene Dzhurinsky
 * Date: 3/18/13
 */
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
