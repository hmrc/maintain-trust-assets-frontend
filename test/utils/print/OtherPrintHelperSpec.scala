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
import models.{NormalMode, UserAnswers}
import models.WhatKindOfAsset.Other
import pages.asset.WhatKindOfAssetPage
import pages.asset.other._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class OtherPrintHelperSpec extends SpecBase {

  private val helper: OtherPrintHelper = injector.instanceOf[OtherPrintHelper]

  private val description: String = "Description"
  private val amount: Long = 100L

  private val answers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage, Other).success.value
    .set(OtherAssetDescriptionPage, description).success.value
    .set(OtherAssetValuePage, amount).success.value

  private val rows: Seq[AnswerRow] = Seq(
    AnswerRow(label = Html(messages("whatKindOfAsset.first.checkYourAnswersLabel")), Html("Other"), WhatKindOfAssetController.onPageLoad().url),
    AnswerRow(label = Html(messages("other.description.checkYourAnswersLabel")), Html(description), OtherAssetDescriptionController.onPageLoad(NormalMode).url),
    AnswerRow(label = Html(messages("other.value.checkYourAnswersLabel", description)), Html("£100"), OtherAssetValueController.onPageLoad(NormalMode).url)
  )

  "OtherPrintHelper" when {

    "generate Other Asset section" when {

      "added" in {

        val result = helper(answers, provisional = true, description)

        result mustBe AnswerSection(
          headingKey = None,
          rows = rows
        )
      }

    }

  }

}
