package sh.oleg.zreader

import org.slf4j.LoggerFactory

/**
 * User: Eugene Dzhurinsky
 * Date: 3/20/13
 */
package object utils {

  private[this] val LOG = LoggerFactory.getLogger("sh.oleg.zreader.utils")

  def safeClose(target: {def close()}) {
    if (target != null) {
      try {
        target.close()
      } catch {
        case e: Exception => LOG.error("Can not close", e)
      }
    }
  }

}
