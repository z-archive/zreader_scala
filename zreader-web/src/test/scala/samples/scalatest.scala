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
package samples

/*
ScalaTest facilitates different styles of testing by providing traits you can mix
together to get the behavior and syntax you prefer.  A few examples are
included here.  For more information, visit:

http://www.scalatest.org/

One way to use ScalaTest is to help make JUnit or TestNG tests more
clear and concise. Here's an example:
*/
import scala.collection.mutable.Stack
import org.scalatest.Assertions
import org.junit.Test

class StackSuite extends Assertions {

  @Test def stackShouldPopValuesIinLastInFirstOutOrder() {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    assert(stack.pop() === 2)
    assert(stack.pop() === 1)
  }

  @Test def stackShouldThrowNoSuchElementExceptionIfAnEmptyStackIsPopped() {
    val emptyStack = new Stack[String]
    intercept[NoSuchElementException] {
      emptyStack.pop()
    }
  }
}

/*
Here's an example of a FunSuite with ShouldMatchers mixed in:
*/
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
@RunWith(classOf[JUnitRunner])
class ListSuite extends FunSuite with ShouldMatchers {

  test("An empty list should be empty") {
    List() should be ('empty)
    Nil should be ('empty)
  }

  test("A non-empty list should not be empty") {
    List(1, 2, 3) should not be ('empty)
    List("fee", "fie", "foe", "fum") should not be ('empty)
  }

  test("A list's length should equal the number of elements it contains") {
    List() should have length (0)
    List(1, 2) should have length (2)
    List("fee", "fie", "foe", "fum") should have length (4)
  }
}

/*
ScalaTest also supports the behavior-driven development style, in which you
combine tests with text that specifies the behavior being tested. Here's
an example whose text output when run looks like:

A Map
- should only contain keys and values that were added to it
- should report its size as the number of key/value pairs it contains
*/
import org.scalatest.FunSpec
import org.scalatest.matchers.MustMatchers

class MapSpec extends FunSpec with MustMatchers {

  describe("A Map") {

    it("should only contain keys and values that were added to it") {
      Map("ho" -> 12) must (not contain key ("hi") and not contain value (13))
      Map("hi" -> 13) must (contain key ("hi") and contain value (13))
    }

    it("should report its size as the number of key/value pairs it contains") {
      Map() must have size (0)
      Map("ho" -> 12) must have size (1)
      Map("hi" -> 13, "ho" -> 12) must have size (2)
    }
  }
}

