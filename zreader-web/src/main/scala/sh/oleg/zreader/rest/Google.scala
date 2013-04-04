package sh.oleg.zreader.rest

import sh.oleg.zreader.auth.google.OAuth2.Callback
import sh.oleg.zreader.auth.Settings.google

object Google extends Callback(google)
