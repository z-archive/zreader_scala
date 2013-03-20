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

package bootstrap.liftweb

import _root_.net.liftweb.http._
import _root_.net.liftweb.sitemap._
import Loc._

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("sh.oleg.zreader")

    // Build SiteMap
    val prefix = "Z-Reader"
    def siteMap() : SiteMap = SiteMap(
      Menu.i("index") / "index",
      Menu.i("login" ) / "login",
      Menu.i("read") / "read",
      Menu.i("settings") / "settings"
    )
    LiftRules.setSiteMapFunc(() => siteMap())
  }
}

