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
import models.WhatKindOfAsset._
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.asset.WhatKindOfAssetPage
import pages.asset.money._
import pages.asset.property_or_land._
import play.twirl.api.Html
import utils.print.PropertyOrLandPrintHelper
import viewmodels.AnswerSection

class PropertyOrLandAnswersHelperSpec extends SpecBase {

  private val mockPrintHelper: PropertyOrLandPrintHelper = mock[PropertyOrLandPrintHelper]
  private val answersHelper: PropertyOrLandAnswersHelper = new PropertyOrLandAnswersHelper(mockPrintHelper)

  private val description: String = "Description"
  private val amount: Long = 100L

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
        .set(PropertyOrLandDescriptionPage(index), description).success.value
        .set(PropertyOrLandTotalValuePage(index), amount).success.value
        .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value

      "interact with PropertyOrLandPrintHelper" in {

        reset(mockPrintHelper)

        when(mockPrintHelper.printSection(any(), any(), any(), any(), any())(any())).thenReturn(AnswerSection())

        val result: Seq[AnswerSection] = answersHelper(userAnswers)

        result mustBe Seq(AnswerSection())

        verify(mockPrintHelper).printSection(any(), any(), any(), any(), any())(any())
      }

      "index headings correctly" in {

        val helper = injector.instanceOf[PropertyOrLandAnswersHelper]

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), Money).success.value
          .set(AssetMoneyValuePage(0), amount).success.value

          .set(WhatKindOfAssetPage(1), PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage(1), false).success.value
          .set(PropertyOrLandDescriptionPage(1), description).success.value
          .set(PropertyOrLandTotalValuePage(1), amount).success.value
          .set(TrustOwnAllThePropertyOrLandPage(1), true).success.value

          .set(WhatKindOfAssetPage(2), PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage(2), false).success.value
          .set(PropertyOrLandDescriptionPage(2), description).success.value
          .set(PropertyOrLandTotalValuePage(2), amount).success.value
          .set(TrustOwnAllThePropertyOrLandPage(2), true).success.value

        val result: Seq[AnswerSection] = helper(userAnswers)

        result.size mustBe 2

        result(0).headingKey mustBe Some("Property or land 1")
        result(0).rows.map(_.answer).contains(Html("Property or land")) mustBe true
        result(1).headingKey mustBe Some("Property or land 2")
        result(1).rows.map(_.answer).contains(Html("Property or land")) mustBe true
      }
    }
  }
}
