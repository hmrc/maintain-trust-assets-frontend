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
import models.UserAnswers
import models.WhatKindOfAsset.Partnership
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.asset.WhatKindOfAssetPage
import pages.asset.partnership._
import utils.print.PartnershipPrintHelper
import viewmodels.AnswerSection

import java.time.LocalDate

class PartnershipAnswersHelperSpec extends SpecBase {

  private val mockPrintHelper: PartnershipPrintHelper = mock[PartnershipPrintHelper]
  private val answersHelper: PartnershipAnswersHelper = new PartnershipAnswersHelper(mockPrintHelper)

  "PartnershipAnswersHelper" when {

    "there are no assets" must {
      "return Nil" in {

        val result: Seq[AnswerSection] = answersHelper(emptyUserAnswers)
        result mustBe Nil
      }
    }

    "there are assets" must {

      val index: Int = 0

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage(index), Partnership).success.value
        .set(PartnershipDescriptionPage(index), "Description").success.value
        .set(PartnershipStartDatePage(index), LocalDate.parse("1996-02-03")).success.value

      "interact with PartnershipPrintHelper" in {

        reset(mockPrintHelper)

        when(mockPrintHelper.printSection(any(), any(), any(), any())(any())).thenReturn(AnswerSection())

        val result: Seq[AnswerSection] = answersHelper(userAnswers)

        result mustBe Seq(AnswerSection())

        verify(mockPrintHelper).printSection(any(), any(), any(), any())(any())
      }
    }
  }
}
