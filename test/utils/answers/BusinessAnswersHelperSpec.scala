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
import models.{UKAddress, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.asset.WhatKindOfAssetPage
import pages.asset.business._
import pages.asset.money._
import play.twirl.api.Html
import utils.print.BusinessPrintHelper
import viewmodels.AnswerSection

class BusinessAnswersHelperSpec extends SpecBase {

  private val mockPrintHelper: BusinessPrintHelper = mock[BusinessPrintHelper]
  private val answersHelper: BusinessAnswersHelper = new BusinessAnswersHelper(mockPrintHelper)

  private val name: String = "Name"
  private val description: String = "Description"
  private val address: UKAddress = UKAddress("Line 1", "Line 2", None, None, "AB1 1AB")
  private val amount: Long = 100L

  "BusinessAnswersHelper" when {

    "there are no assets" must {
      "return Nil" in {

        val result: Seq[AnswerSection] = answersHelper(emptyUserAnswers)
        result mustBe Nil
      }
    }

    "there are assets" must {

      "interact with BusinessPrintHelper" in {

        reset(mockPrintHelper)

        when(mockPrintHelper.printSection(any(), any(), any(), any(), any())(any())).thenReturn(AnswerSection())

        val index: Int = 0

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(index), Business).success.value
          .set(BusinessNamePage(index), name).success.value
          .set(BusinessDescriptionPage(index), description).success.value
          .set(BusinessAddressUkYesNoPage(index), true).success.value
          .set(BusinessUkAddressPage(index), address).success.value
          .set(BusinessValuePage(index), amount).success.value

        val result: Seq[AnswerSection] = answersHelper(userAnswers)

        result mustBe Seq(AnswerSection())

        verify(mockPrintHelper).printSection(any(), any(), any(), any(), any())(any())
      }

      "index headings correctly" in {

        val helper = injector.instanceOf[BusinessAnswersHelper]

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), Money).success.value
          .set(AssetMoneyValuePage(0), amount).success.value

          .set(WhatKindOfAssetPage(1), Business).success.value
          .set(BusinessNamePage(1), name).success.value
          .set(BusinessDescriptionPage(1), description).success.value
          .set(BusinessAddressUkYesNoPage(1), true).success.value
          .set(BusinessUkAddressPage(1), address).success.value
          .set(BusinessValuePage(1), amount).success.value

          .set(WhatKindOfAssetPage(2), Business).success.value
          .set(BusinessNamePage(2), name).success.value
          .set(BusinessDescriptionPage(2), description).success.value
          .set(BusinessAddressUkYesNoPage(2), true).success.value
          .set(BusinessUkAddressPage(2), address).success.value
          .set(BusinessValuePage(2), amount).success.value

        val result: Seq[AnswerSection] = helper(userAnswers)

        result.size mustBe 2

        result(0).headingKey mustBe Some("Business 1")
        result(0).rows.map(_.answer).contains(Html("Business")) mustBe true
        result(0).rows.map(_.labelArg).contains(name) mustBe true
        result(1).headingKey mustBe Some("Business 2")
        result(1).rows.map(_.answer).contains(Html("Business")) mustBe true
        result(1).rows.map(_.labelArg).contains(name) mustBe true
      }
    }
  }
}
