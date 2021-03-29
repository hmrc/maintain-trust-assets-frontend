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
import controllers.asset.routes.WhatKindOfAssetController
import controllers.asset.shares.routes._
import models.WhatKindOfAsset.Shares
import models.{NormalMode, ShareClass, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import pages.asset.shares._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class SharesPrintHelperSpec extends SpecBase {

  private val helper: SharesPrintHelper = injector.instanceOf[SharesPrintHelper]
  private val index: Int = 0
  private val name: String = "Name"
  private val quantity: Long = 200L
  private val amount: Long = 100L

  private val nonPortfolioAnswers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage, Shares).success.value
    .set(SharesInAPortfolioPage, false).success.value
    .set(ShareCompanyNamePage, name).success.value
    .set(SharesOnStockExchangePage, true).success.value
    .set(ShareClassPage, ShareClass.Ordinary).success.value
    .set(ShareQuantityInTrustPage, quantity).success.value
    .set(ShareValueInTrustPage, amount).success.value

  private val portfolioAnswers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage, Shares).success.value
    .set(SharesInAPortfolioPage, true).success.value
    .set(SharePortfolioNamePage, name).success.value
    .set(SharePortfolioOnStockExchangePage, true).success.value
    .set(SharePortfolioQuantityInTrustPage, quantity).success.value
    .set(SharePortfolioValueInTrustPage, amount).success.value

  private val nonPortfolioRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Shares"), Some(WhatKindOfAssetController.onPageLoad(index).url)),
    AnswerRow("shares.inAPortfolioYesNo.checkYourAnswersLabel", Html("No"), Some(SharesInAPortfolioController.onPageLoad(NormalMode).url)),
    AnswerRow("shares.companyName.checkYourAnswersLabel", Html(name), Some(ShareCompanyNameController.onPageLoad(NormalMode).url)),
    AnswerRow("shares.onStockExchangeYesNo.checkYourAnswersLabel", Html("Yes"), Some(SharesOnStockExchangeController.onPageLoad(NormalMode).url)),
    AnswerRow("shares.class.checkYourAnswersLabel", Html("Ordinary"), Some(ShareClassController.onPageLoad(NormalMode).url)),
    AnswerRow("shares.quantityInTrust.checkYourAnswersLabel", Html("200"), Some(ShareQuantityInTrustController.onPageLoad(NormalMode).url)),
    AnswerRow("shares.valueInTrust.checkYourAnswersLabel", Html("£100"), Some(ShareValueInTrustController.onPageLoad(NormalMode).url))
  )

  private val portfolioRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Shares"), Some(WhatKindOfAssetController.onPageLoad(index).url)),
    AnswerRow("shares.inAPortfolioYesNo.checkYourAnswersLabel", Html("Yes"), Some(SharesInAPortfolioController.onPageLoad(NormalMode).url)),
    AnswerRow("shares.portfolioName.checkYourAnswersLabel", Html(name), Some(SharePortfolioNameController.onPageLoad(NormalMode).url)),
    AnswerRow("shares.portfolioOnStockExchangeYesNo.checkYourAnswersLabel", Html("Yes"), Some(SharePortfolioOnStockExchangeController.onPageLoad(NormalMode).url)),
    AnswerRow("shares.portfolioQuantityInTrust.checkYourAnswersLabel", Html("200"), Some(SharePortfolioQuantityInTrustController.onPageLoad(NormalMode).url)),
    AnswerRow("shares.portfolioValueInTrust.checkYourAnswersLabel", Html("£100"), Some(SharePortfolioValueInTrustController.onPageLoad(NormalMode).url))
  )

  "SharesPrintHelper" when {

    "generate shares Asset section" when {

      "added" when {

        "non-portfolio asset" in {

          val result = helper(nonPortfolioAnswers, provisional = true, name)

          result mustBe AnswerSection(
            headingKey = None,
            rows = nonPortfolioRows
          )
        }

        "portfolio asset" in {

          val result = helper(portfolioAnswers, provisional = true, name)

          result mustBe AnswerSection(
            headingKey = None,
            rows = portfolioRows
          )
        }
      }

    }

  }
}
