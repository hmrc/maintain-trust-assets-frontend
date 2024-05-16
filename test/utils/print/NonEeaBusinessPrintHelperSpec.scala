/*
 * Copyright 2024 HM Revenue & Customs
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

package utils.print

import base.SpecBase
import controllers.asset.noneeabusiness.add.routes._
import controllers.asset.noneeabusiness.routes._
import models.{NonUkAddress, NormalMode, UserAnswers}
import pages.asset.noneeabusiness._
import pages.asset.noneeabusiness.add.StartDatePage
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class NonEeaBusinessPrintHelperSpec extends SpecBase {

  private val helper: NonEeaBusinessPrintHelper = injector.instanceOf[NonEeaBusinessPrintHelper]

  private val name: String = "Name"
  private val country: String = "FR"
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, country)
  private val date: LocalDate = LocalDate.parse("1996-02-03")

  private val userAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(NonUkAddressPage, nonUkAddress).success.value
    .set(GoverningCountryPage, country).success.value
    .set(StartDatePage, date).success.value

  private val assetRows: Seq[AnswerRow] = Seq(
    AnswerRow(label = messages("nonEeaBusiness.name.checkYourAnswersLabel"), Html(name), NameController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("nonEeaBusiness.internationalAddress.checkYourAnswersLabel", name), Html("Line 1<br />Line 2<br />France"), InternationalAddressController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("nonEeaBusiness.governingCountry.checkYourAnswersLabel", name), Html("France"), GoverningCountryController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("nonEeaBusiness.startDate.checkYourAnswersLabel", name), Html("3 February 1996"), StartDateController.onPageLoad().url)
  )

  "NonEeaBusinessPrintHelper" when {

    "generate Noneebusiness Asset section" when {

      "added" in {

        val result = helper(userAnswers.copy(isTaxable = true), provisional = true, name)

        result mustBe AnswerSection(
          headingKey = None,
          rows = assetRows
        )

      }
    }
  }
}
