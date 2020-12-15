/*
 * Copyright 2020 HM Revenue & Customs
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
import models.{UKAddress, UserAnswers}
import models.WhatKindOfAsset.Business
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.asset.WhatKindOfAssetPage
import pages.asset.business._
import utils.print.BusinessPrintHelper
import viewmodels.AnswerSection

class BusinessAnswersHelperSpec extends SpecBase {

  private val mockPrintHelper: BusinessPrintHelper = mock[BusinessPrintHelper]
  private val answersHelper: BusinessAnswersHelper = new BusinessAnswersHelper(mockPrintHelper)

  private val name: String = "Name"

  "BusinessAnswersHelper" when {

    "there are no assets" must {
      "return Nil" in {

        val result: Seq[AnswerSection] = answersHelper(emptyUserAnswers, name)
        result mustBe Nil
      }
    }

    "there are assets" must {

      val index: Int = 0

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage(index), Business).success.value
        .set(BusinessNamePage(index), "Name").success.value
        .set(BusinessDescriptionPage(index), "Description").success.value
        .set(BusinessAddressUkYesNoPage(index), true).success.value
        .set(BusinessUkAddressPage(index), UKAddress("Line 1", "Line 2", None, None, "AB1 1AB")).success.value
        .set(BusinessValuePage(index), 100L).success.value

      "interact with BusinessPrintHelper" in {

        reset(mockPrintHelper)

        when(mockPrintHelper.printSection(any(), any(), any(), any())(any())).thenReturn(AnswerSection())

        val result: Seq[AnswerSection] = answersHelper(userAnswers, name)

        result mustBe Seq(AnswerSection())

        verify(mockPrintHelper).printSection(any(), any(), any(), any())(any())
      }
    }
  }
}
