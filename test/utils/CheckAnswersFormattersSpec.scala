/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import base.SpecBase
import models.{NonUkAddress, UkAddress}
import play.api.i18n.{Lang, MessagesImpl}
import play.twirl.api.Html
import utils.CheckAnswersFormatters._

import java.time.LocalDate

class CheckAnswersFormattersSpec extends SpecBase {

  private val checkAnswersFormatters: CheckAnswersFormatters = injector.instanceOf[CheckAnswersFormatters]

  "CheckAnswersFormatters" when {

    ".formatDate" when {

      def messages(langCode: String): MessagesImpl = {
        val lang: Lang = Lang(langCode)
        MessagesImpl(lang, messagesApi)
      }

      val recentDate: LocalDate = LocalDate.parse("2015-01-25")

      "in English mode" must {
        "format date in English" when {
          "recent date" in {
            val result: Html = checkAnswersFormatters.formatDate(recentDate)(messages("en"))
            result mustBe Html("25 January 2015")
          }
        }
      }

      "in Welsh mode" must {
        "format date in Welsh" when {
          "recent date" in {
            val result: Html = checkAnswersFormatters.formatDate(recentDate)(messages("cy"))
            result mustBe Html("25 Ionawr 2015")
          }
        }
      }
    }

    ".yesOrNo" when {

      "true" must {
        "return Yes" in {
          val result: Html = yesOrNo(answer = true)
          result mustBe Html("Yes")
        }
      }

      "false" must {
        "return No" in {
          val result: Html = yesOrNo(answer = false)
          result mustBe Html("No")
        }
      }
    }

    ".currency" must {
      "prepend £ symbol to value" in {
        val result: Html = currency("100")
        result mustBe Html("£100")
      }
    }

    ".addressFormatter" when {

      "UK address" must {
        "return formatted address" when {

          "lines 3 and 4 provided" in {
            val address: UkAddress = UkAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "AB1 1AB")
            val result: Html = checkAnswersFormatters.addressFormatter(address)
            result mustBe Html("Line 1<br />Line 2<br />Line 3<br />Line 4<br />AB1 1AB")
          }

          "lines 3 and 4 not provided" in {
            val address: UkAddress = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB")
            val result: Html = checkAnswersFormatters.addressFormatter(address)
            result mustBe Html("Line 1<br />Line 2<br />AB1 1AB")
          }
        }
      }

      "non-UK address" must {
        "return formatted address" when {

          "line 3 provided" in {
            val address: NonUkAddress = NonUkAddress("Line 1", "Line 2", Some("Line 3"), "FR")
            val result: Html = checkAnswersFormatters.addressFormatter(address)
            result mustBe Html("Line 1<br />Line 2<br />Line 3<br />France")
          }

          "line 3 not provided" in {
            val address: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "FR")
            val result: Html = checkAnswersFormatters.addressFormatter(address)
            result mustBe Html("Line 1<br />Line 2<br />France")
          }
        }
      }
    }
  }
}
