/*
 * Copyright 2024 HM Revenue & Customs
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
    AnswerRow(label = messages("whatKindOfAsset.checkYourAnswersLabel"), Html("Shares"), WhatKindOfAssetController.onPageLoad().url),
    AnswerRow(label = messages("shares.inAPortfolioYesNo.checkYourAnswersLabel"), Html("No"), SharesInAPortfolioController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("shares.companyName.checkYourAnswersLabel"), Html(name), ShareCompanyNameController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("shares.onStockExchangeYesNo.checkYourAnswersLabel", name), Html("Yes"), SharesOnStockExchangeController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("shares.class.checkYourAnswersLabel", name), Html("Ordinary"), ShareClassController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("shares.quantityInTrust.checkYourAnswersLabel", name), Html("200"), ShareQuantityInTrustController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("shares.valueInTrust.checkYourAnswersLabel", name), Html("£100"), ShareValueInTrustController.onPageLoad(NormalMode).url)
  )

  private val portfolioRows: Seq[AnswerRow] = Seq(
    AnswerRow(label = messages("whatKindOfAsset.checkYourAnswersLabel"), Html("Shares"), WhatKindOfAssetController.onPageLoad().url),
    AnswerRow(label = messages("shares.inAPortfolioYesNo.checkYourAnswersLabel"), Html("Yes"), SharesInAPortfolioController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("shares.portfolioName.checkYourAnswersLabel"), Html(name), SharePortfolioNameController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("shares.portfolioOnStockExchangeYesNo.checkYourAnswersLabel", name), Html("Yes"), SharePortfolioOnStockExchangeController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("shares.portfolioQuantityInTrust.checkYourAnswersLabel", name), Html("200"), SharePortfolioQuantityInTrustController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("shares.portfolioValueInTrust.checkYourAnswersLabel", name), Html("£100"), SharePortfolioValueInTrustController.onPageLoad(NormalMode).url)
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
