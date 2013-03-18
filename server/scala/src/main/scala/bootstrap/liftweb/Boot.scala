package bootstrap.liftweb

import _root_.net.liftweb.http._
import _root_.net.liftweb.sitemap._

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("sh.oleg.zreader")

    // use HTML5 by default
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // Build SiteMap
    val entries = Menu(Loc("Home", List("index"), "Home")) :: Nil
    LiftRules.setSiteMap(SiteMap(entries:_*))
  }
}

