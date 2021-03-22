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

package utils.print

import base.SpecBase
import controllers.asset.other.routes._
import controllers.asset.routes.WhatKindOfAssetController
import models.UserAnswers
import models.WhatKindOfAsset.Other
import pages.asset.WhatKindOfAssetPage
import pages.asset.other._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class OtherPrintHelperSpec extends SpecBase {

  private val helper: OtherPrintHelper = injector.instanceOf[OtherPrintHelper]

  private val index: Int = 0

  private val heading: String = s"Other ${index + 1}"

  private val description: String = "Description"
  private val amount: Long = 100L

  private val answers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage(index), Other).success.value
    .set(OtherAssetDescriptionPage(index), description).success.value
    .set(OtherAssetValuePage(index), amount).success.value

  private val rows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Other"), Some(WhatKindOfAssetController.onPageLoad(index).url)),
    AnswerRow("other.description.checkYourAnswersLabel", Html(description), Some(OtherAssetDescriptionController.onPageLoad(index).url)),
    AnswerRow("other.value.checkYourAnswersLabel", Html("£100"), Some(OtherAssetValueController.onPageLoad(index).url))
  )

  "OtherPrintHelper" when {

    "printSection" must {
      "render answer section with heading" in {

        val result: AnswerSection = helper.printSection(
          userAnswers = answers,
          index = index,
          specificIndex = index
        )

        result mustBe AnswerSection(
          headingKey = Some(heading),
          rows = rows
        )
      }
    }

    "checkDetailsSection" must {
      "render answer section without heading" in {

        val result: Seq[AnswerSection] = helper.checkDetailsSection(
          userAnswers = answers,
          index = index
        )

        result mustBe Seq(AnswerSection(
          headingKey = None,
          rows = rows
        ))
      }
    }
  }

}
