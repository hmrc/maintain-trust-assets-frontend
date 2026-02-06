/*
 * Copyright 2026 HM Revenue & Customs
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
import controllers.asset.partnership.routes._
import controllers.asset.routes.WhatKindOfAssetController
import models.WhatKindOfAsset.Partnership
import models.{NormalMode, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import pages.asset.partnership._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class PartnershipPrintHelperSpec extends SpecBase {

  private val helper: PartnershipPrintHelper = injector.instanceOf[PartnershipPrintHelper]

  private val description: String = "Description"
  private val date: LocalDate     = LocalDate.parse("1996-02-03")

  private val answers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage(index), Partnership)
    .success
    .value
    .set(PartnershipDescriptionPage(index), description)
    .success
    .value
    .set(PartnershipStartDatePage(index), date)
    .success
    .value

  private val rows: Seq[AnswerRow] = Seq(
    AnswerRow(
      label = messages("whatKindOfAsset.checkYourAnswersLabel"),
      Html("Partnership"),
      WhatKindOfAssetController.onPageLoad(index).url
    ),
    AnswerRow(
      label = messages("partnership.description.checkYourAnswersLabel"),
      Html(description),
      PartnershipDescriptionController.onPageLoad(index, NormalMode).url
    ),
    AnswerRow(
      label = messages("partnership.startDate.checkYourAnswersLabel", description),
      Html("3 February 1996"),
      PartnershipStartDateController.onPageLoad(index, NormalMode).url
    )
  )

  "PartnershipPrintHelper" when {

    "generate Partnership Asset section" when {

      "added" in {

        val result = helper(answers, index, provisional = true, description)

        result mustBe AnswerSection(
          headingKey = None,
          rows = rows
        )
      }

    }

  }

}
