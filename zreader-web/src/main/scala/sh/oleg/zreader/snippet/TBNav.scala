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
package sh.oleg.zreader.snippet

import scala.xml._
import scala.xml.transform._
import net.liftweb.util.Helpers._
import net.liftweb.common.Logger
import net.liftweb.util.{True, IterableFunc}

/**
 * TBNav - class for integration Twitter Bootstrap navbar (Navigration Bar) and Lift SiteMap (i.e. Menu.builder)
 *
 * I got it from: http://tech.damianhelme.com/twitter-bootstrap-navbar-dropdowns-and-lifts/
 * */
object TBNav extends Logger {

  /**
  Transforms the XML produced by Menu.build such that any menus defined as:
      Menu("Test") / "test" >> LocGroup("test") >> PlaceHolder submenus (
      Menu("Test 2") / "test2",
      Menu("Test 3") / "test3"
    ),
    or
    Menu("Test") / "test" >> LocGroup("test") >> submenus (
      Menu("Test 2") / "test2",
      Menu("Test 3") / "test3"
    ),
    will be transformed into Twitter Bootstrap dropdown menus
    */
  def menuToTBNav( in: NodeSeq ) : NodeSeq = {

    object t1 extends RewriteRule {
      override def transform(n: Node): Seq[Node] = n match {

        // removes the white space which appears between elements
        case Text(text) if ( text.matches("\\s+") ) => NodeSeq.Empty

        /* matches xml of the format:
          *<li>
             <span>Test</span>
             <ul>
              <li>
                 <a href="/test2">Test 2</a>
              </li>
              <li>
                 <a href="/test3">Test 3</a>
              </li>
             </ul>
            </li>
        and transforms it to:
          <li class="dropdown" >
             <a class="dropdown-toggle" data-toggle="dropdown" >Test<b class="caret"></b></a>
             <ul class="dropdown-menu">
                <li>
                   <a href="/test2">Test 2</a>
                </li>
                <li>
                   <a href="/test3">Test 3</a>
                </li>
             </ul>
          </li>

          */
        case li @ Elem(liPrefix, "li", liAttribs, liScope,
        span @ Elem(spanPrefix,"span",spanAttribs,spanScope,spanChildren @ _*),
        ul @ Elem(ulPrefix,"ul",ulAttribs,ulScope,ulChildren @ _*),
        other @ _* )  => {

          // create a new node seq with modified attributes
          Elem(liPrefix, "li", newLiAttribs(liAttribs), liScope, true,
            Elem(spanPrefix, "a", newAAttribs(spanAttribs), spanScope, true, newAChildren(spanChildren): _*) ++
              Elem(ulPrefix, "ul", newUlAttribs(ulAttribs), ulScope, true, ulChildren: _*) ++
              other: _*)
        }

        /* matches xml of the format:
           *<li>
              <a href="/test">Test</a>
              <ul>
               <li>
                  <a href="/test2">Test 2</a>
               </li>
               <li>
                  <a href="/test3">Test 3</a>
               </li>
              </ul>
             </li>

         and transforms it to:

           <li class="dropdown" >
              <a class="dropdown-toggle" data-toggle="dropdown" >Test<b class="caret"></b></a>
              <ul class="dropdown-menu">
                 <li>
                    <a href="/test2">Test 2</a>
                 </li>
                 <li>
                    <a href="/test3">Test 3</a>
                 </li>
              </ul>
           </li>

           */
        case li @ Elem(liPrefix, "li", liAttribs, liScope,
        a @ Elem(aPrefix,"a",aAttribs,aScope,aChildren @ _*),
        ul @ Elem(ulPrefix,"ul",ulAttribs,ulScope,ulChildren @ _*),
        other @ _* )  => {

          // create a new node seq with modified attributes
          Elem(liPrefix,"li",newLiAttribs(liAttribs),liScope, true,
            Elem(aPrefix, "a", newAAttribs(aAttribs), aScope, true, newAChildren(aChildren): _*) ++
              Elem(ulPrefix, "ul", newUlAttribs(ulAttribs), ulScope, true, ulChildren: _*) ++
              other: _*)
        }
        case other @ _ => other
      }
    }

    // debug("menuToTBNav received: " + new PrettyPrinter(80,3).formatNodes(in))
    object rt1 extends RuleTransformer(t1)
    val out = rt1.transform(in)
    // debug("menuToTBNav out: " + new PrettyPrinter(80,3).formatNodes(out))
    out
  }

  /*
   * an attempt at using CSS selectors rather than XML Transform - TBC
  def menuToTBNav(in: NodeSeq) : NodeSeq = {
     def testNode(ns: NodeSeq, cssSel: String): Boolean = {
    var ret = false // does the NodeSeq have any nodes that match the CSS Selector
    (cssSel #> ((ignore: NodeSeq) => {ret = true; NodeSeq.Empty}))(ns)
    ret
  }
     def childHasUI(ns: NodeSeq) : Boolean = {
       true
     }

     val f = "li [class+]" #>
       (((ns: NodeSeq) => Some("dropdown").filter(ignore => childHasUI(ns))): IterableFunc )

     f(in)
  }
  */

  // utility methods to add the Bootstrap classes to existing attributes
  def newLiAttribs(oldAttribs: MetaData) =  appendToClass(oldAttribs,"dropdown")
  def newAAttribs(oldAttribs: MetaData) = appendToClass(oldAttribs,"dropdown-toggle")
    .append("data-toggle" -> "dropdown")
  def newUlAttribs(oldAttribs: MetaData) = appendToClass(oldAttribs,"dropdown-menu")
  def newAChildren(oldChildren: NodeSeq) = oldChildren ++ <b class="caret"></b>



  // append a new value to the class attribute if one already exists, otherwise create a new class
  // with the given value
  def appendToClass(attribs: MetaData, newClass: String ) : MetaData = {
    // Note that MetaData.get("class") returns a Option[Seq[Node]] , not Option[Node] as might be expected
    // for an explanation of why see the scala-xml book:
    val oldClass : Option[String] = attribs.get("class").map(_.mkString).filterNot(_ == "")
    val resultingClass = oldClass.map( _.trim + " ").getOrElse("") + newClass
    attribs.append("class" -> resultingClass)
  }

}