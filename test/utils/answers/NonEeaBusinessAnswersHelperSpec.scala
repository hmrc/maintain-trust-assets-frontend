/*
 * Copyright 2021 HM Revenue & Customs
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

package utils.answers

import base.SpecBase
import models.WhatKindOfAsset._
import models.{InternationalAddress, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.asset.WhatKindOfAssetPage
import pages.asset.money._
import pages.asset.noneeabusiness._
import play.twirl.api.Html
import utils.print.NonEeaBusinessPrintHelper
import viewmodels.AnswerSection

import java.time.LocalDate

class NonEeaBusinessAnswersHelperSpec extends SpecBase {

  private val mockPrintHelper: NonEeaBusinessPrintHelper = mock[NonEeaBusinessPrintHelper]
  private val answersHelper: NonEeaBusinessAnswersHelper = new NonEeaBusinessAnswersHelper(mockPrintHelper)

  private val name: String = "Name"
  private val country: String = "FR"
  private val address: InternationalAddress = InternationalAddress("Line 1", "Line 2", None, country)
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val amount: Long = 100L

  "NonEeaBusinessAnswersHelper" when {

    "there are no assets" must {
      "return Nil" in {

        val result: Seq[AnswerSection] = answersHelper(emptyUserAnswers)
        result mustBe Nil
      }
    }

    "there are assets" must {

      "interact with NonEeaBusinessPrintHelper" in {

        reset(mockPrintHelper)

        when(mockPrintHelper.printSection(any(), any(), any(), any(), any())(any())).thenReturn(AnswerSection())

        val index: Int = 0

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(index), NonEeaBusiness).success.value
          .set(NamePage(index), name).success.value
          .set(InternationalAddressPage(index), address).success.value
          .set(GoverningCountryPage(index), country).success.value
          .set(StartDatePage(index), date).success.value

        val result: Seq[AnswerSection] = answersHelper(userAnswers)

        result mustBe Seq(AnswerSection())

        verify(mockPrintHelper).printSection(any(), any(), any(), any(), any())(any())
      }

      "index headings correctly" in {

        val helper = injector.instanceOf[NonEeaBusinessAnswersHelper]

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), Money).success.value
          .set(AssetMoneyValuePage(0), amount).success.value

          .set(WhatKindOfAssetPage(1), NonEeaBusiness).success.value
          .set(NamePage(1), name).success.value
          .set(InternationalAddressPage(1), address).success.value
          .set(GoverningCountryPage(1), country).success.value
          .set(StartDatePage(1), date).success.value

          .set(WhatKindOfAssetPage(2), NonEeaBusiness).success.value
          .set(NamePage(2), name).success.value
          .set(InternationalAddressPage(2), address).success.value
          .set(GoverningCountryPage(2), country).success.value
          .set(StartDatePage(2), date).success.value

        val result: Seq[AnswerSection] = helper(userAnswers)

        result.size mustBe 2

        result(0).headingKey mustBe Some("Non-EEA Company 1")
        result(0).rows.map(_.answer).contains(Html("Non-EEA Company")) mustBe true
        result(0).rows.map(_.labelArg).contains(name) mustBe true
        result(1).headingKey mustBe Some("Non-EEA Company 2")
        result(1).rows.map(_.answer).contains(Html("Non-EEA Company")) mustBe true
        result(1).rows.map(_.labelArg).contains(name) mustBe true
      }
    }
  }
}
