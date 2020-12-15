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
import models.WhatKindOfAsset.PropertyOrLand
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._
import utils.print.PropertyOrLandPrintHelper
import viewmodels.AnswerSection

class PropertyOrLandAnswersHelperSpec extends SpecBase {

  private val mockPrintHelper: PropertyOrLandPrintHelper = mock[PropertyOrLandPrintHelper]
  private val answersHelper: PropertyOrLandAnswersHelper = new PropertyOrLandAnswersHelper(mockPrintHelper)

  "PropertyOrLandAnswersHelper" when {

    "there are no assets" must {
      "return Nil" in {

        val result: Seq[AnswerSection] = answersHelper(emptyUserAnswers)
        result mustBe Nil
      }
    }

    "there are assets" must {

      val index: Int = 0

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
        .set(PropertyOrLandAddressYesNoPage(index), false).success.value
        .set(PropertyOrLandDescriptionPage(index), "Description").success.value
        .set(PropertyOrLandTotalValuePage(index), 100L).success.value
        .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value

      "interact with PropertyOrLandPrintHelper" in {

        reset(mockPrintHelper)

        when(mockPrintHelper.printSection(any(), any(), any(), any())(any())).thenReturn(AnswerSection())

        val result: Seq[AnswerSection] = answersHelper(userAnswers)

        result mustBe Seq(AnswerSection())

        verify(mockPrintHelper).printSection(any(), any(), any(), any())(any())
      }
    }
  }
}
