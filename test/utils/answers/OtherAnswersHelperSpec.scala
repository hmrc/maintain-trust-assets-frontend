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
import pages.asset.other._
import play.twirl.api.Html
import utils.print.OtherPrintHelper
import viewmodels.AnswerSection

class OtherAnswersHelperSpec extends SpecBase {

  private val mockPrintHelper: OtherPrintHelper = mock[OtherPrintHelper]
  private val answersHelper: OtherAnswersHelper = new OtherAnswersHelper(mockPrintHelper)

  private val description: String = "Description"
  private val amount: Long = 100L

  "OtherAnswersHelper" when {

    "there are no assets" must {
      "return Nil" in {

        val result: Seq[AnswerSection] = answersHelper(emptyUserAnswers)
        result mustBe Nil
      }
    }

    "there are assets" must {

      val index: Int = 0

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage(index), Other).success.value
        .set(OtherAssetDescriptionPage(index), description).success.value
        .set(OtherAssetValuePage(index), amount).success.value

      "interact with OtherPrintHelper" in {

        reset(mockPrintHelper)

        when(mockPrintHelper.printSection(any(), any(), any(), any(), any())(any())).thenReturn(AnswerSection())

        val result: Seq[AnswerSection] = answersHelper(userAnswers)

        result mustBe Seq(AnswerSection())

        verify(mockPrintHelper).printSection(any(), any(), any(), any(), any())(any())
      }

      "index headings correctly" in {

        val helper = injector.instanceOf[OtherAnswersHelper]

        val userAnswers: UserAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), Money).success.value
          .set(AssetMoneyValuePage(0), amount).success.value

          .set(WhatKindOfAssetPage(1), Other).success.value
          .set(OtherAssetDescriptionPage(1), description).success.value
          .set(OtherAssetValuePage(1), amount).success.value

          .set(WhatKindOfAssetPage(2), Other).success.value
          .set(OtherAssetDescriptionPage(2), description).success.value
          .set(OtherAssetValuePage(2), amount).success.value

        val result: Seq[AnswerSection] = helper(userAnswers)

        result.size mustBe 2

        result(0).headingKey mustBe Some("Other 1")
        result(0).rows.map(_.answer).contains(Html("Other")) mustBe true
        result(1).headingKey mustBe Some("Other 2")
        result(1).rows.map(_.answer).contains(Html("Other")) mustBe true
      }
    }
  }
}
