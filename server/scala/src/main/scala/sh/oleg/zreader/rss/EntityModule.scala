package sh.oleg.zreader.rss

/**
 * User: Eugene Dzhurinsky
 * Date: 3/18/13
 *
 * Holds information about entities being used in the project.
 */
object EntityModule {

  case class FeedItem(content: String, permLink: String, published: Long)

  case class Feed(items: Iterable[FeedItem])

}
