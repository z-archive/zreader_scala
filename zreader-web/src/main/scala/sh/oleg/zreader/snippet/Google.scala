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

package sh.oleg.zreader.snippet

import sh.oleg.zreader.auth.google.oauth2.Login._

object Google {
  private val snippet = new Snippet(
    redirectUri="http://localhost:8080/oauth2callback",
    clientId="855033541488.apps.googleusercontent.com").
    withState("/profile").
    withAccessType(offline).
    withResponseType(code).
    withApprovalPrompt(force)
  val render = snippet.render
}
