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

package utils.print

import base.SpecBase
import controllers.asset.routes.WhatKindOfAssetController
import controllers.asset.shares.routes._
import models.WhatKindOfAsset.Shares
import models.{ShareClass, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import pages.asset.shares._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class SharesPrintHelperSpec extends SpecBase {

  private val helper: SharesPrintHelper = injector.instanceOf[SharesPrintHelper]

  private val index: Int = 0

  private val heading: String = s"Share ${index + 1}"

  private val name: String = "Name"
  private val quantity: String = "100"
  private val amount: Long = 100L

  private val nonPortfolioAnswers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage(index), Shares).success.value
    .set(SharesInAPortfolioPage(index), false).success.value
    .set(ShareCompanyNamePage(index), name).success.value
    .set(SharesOnStockExchangePage(index), true).success.value
    .set(ShareClassPage(index), ShareClass.Ordinary).success.value
    .set(ShareQuantityInTrustPage(index), quantity).success.value
    .set(ShareValueInTrustPage(index), amount).success.value

  private val portfolioAnswers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage(index), Shares).success.value
    .set(SharesInAPortfolioPage(index), true).success.value
    .set(SharePortfolioNamePage(index), name).success.value
    .set(SharePortfolioOnStockExchangePage(index), true).success.value
    .set(SharePortfolioQuantityInTrustPage(index), quantity).success.value
    .set(SharePortfolioValueInTrustPage(index), amount).success.value

  private val nonPortfolioRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Shares"), Some(WhatKindOfAssetController.onPageLoad(index, fakeDraftId).url)),
    AnswerRow("shares.inAPortfolioYesNo.checkYourAnswersLabel", Html("No"), Some(SharesInAPortfolioController.onPageLoad(index, draftId).url)),
    AnswerRow("shares.companyName.checkYourAnswersLabel", Html(name), Some(ShareCompanyNameController.onPageLoad(index, draftId).url)),
    AnswerRow("shares.onStockExchangeYesNo.checkYourAnswersLabel", Html("Yes"), Some(SharesOnStockExchangeController.onPageLoad(index, draftId).url)),
    AnswerRow("shares.class.checkYourAnswersLabel", Html("Ordinary"), Some(ShareClassController.onPageLoad(index, draftId).url)),
    AnswerRow("shares.quantityInTrust.checkYourAnswersLabel", Html("100"), Some(ShareQuantityInTrustController.onPageLoad(index, draftId).url)),
    AnswerRow("shares.valueInTrust.checkYourAnswersLabel", Html("£100"), Some(ShareValueInTrustController.onPageLoad(index, draftId).url))
  )

  private val portfolioRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Shares"), Some(WhatKindOfAssetController.onPageLoad(index, fakeDraftId).url)),
    AnswerRow("shares.inAPortfolioYesNo.checkYourAnswersLabel", Html("Yes"), Some(SharesInAPortfolioController.onPageLoad(index, draftId).url)),
    AnswerRow("shares.portfolioName.checkYourAnswersLabel", Html(name), Some(SharePortfolioNameController.onPageLoad(index, draftId).url)),
    AnswerRow("shares.portfolioOnStockExchangeYesNo.checkYourAnswersLabel", Html("Yes"), Some(SharePortfolioOnStockExchangeController.onPageLoad(index, draftId).url)),
    AnswerRow("shares.portfolioQuantityInTrust.checkYourAnswersLabel", Html("100"), Some(SharePortfolioQuantityInTrustController.onPageLoad(index, draftId).url)),
    AnswerRow("shares.portfolioValueInTrust.checkYourAnswersLabel", Html("£100"), Some(SharePortfolioValueInTrustController.onPageLoad(index, draftId).url))
  )

  "SharesPrintHelper" when {

    "printSection" must {
      "render answer section with heading" when {

        "non-portfolio asset" in {

          val result: AnswerSection = helper.printSection(
            userAnswers = nonPortfolioAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe AnswerSection(
            headingKey = Some(heading),
            rows = nonPortfolioRows
          )
        }

        "portfolio asset" in {

          val result: AnswerSection = helper.printSection(
            userAnswers = portfolioAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe AnswerSection(
            headingKey = Some(heading),
            rows = portfolioRows
          )
        }
      }
    }

    "checkDetailsSection" must {
      "render answer section without heading" when {

        "non-portfolio asset" in {

          val result: Seq[AnswerSection] = helper.checkDetailsSection(
            userAnswers = nonPortfolioAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe Seq(AnswerSection(
            headingKey = None,
            rows = nonPortfolioRows
          ))
        }

        "portfolio asset" in {

          val result: Seq[AnswerSection] = helper.checkDetailsSection(
            userAnswers = portfolioAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe Seq(AnswerSection(
            headingKey = None,
            rows = portfolioRows
          ))
        }
      }
    }
  }
}
