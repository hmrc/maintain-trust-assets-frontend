/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.asset.shares.routes._
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.asset.shares._
import play.api.i18n.Messages
import utils.AnswerRowConverter
import viewmodels.{AnswerRow, AnswerSection}
import javax.inject.Inject

class SharesPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, name: String)(implicit messages: Messages): AnswerSection = {

    val bound: answerRowConverter.Bound = answerRowConverter.bind(userAnswers, name)

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (provisional) NormalMode else CheckMode
      Seq(
        bound.assetTypeQuestion(0),
        bound.yesNoQuestion(SharesInAPortfolioPage, "shares.inAPortfolioYesNo", SharesInAPortfolioController.onPageLoad(mode).url),
        bound.stringQuestion(ShareCompanyNamePage, "shares.companyName", ShareCompanyNameController.onPageLoad(mode).url),
        bound.stringQuestion(SharePortfolioNamePage, "shares.portfolioName", SharePortfolioNameController.onPageLoad(mode).url),
        bound.yesNoQuestion(SharesOnStockExchangePage, "shares.onStockExchangeYesNo", SharesOnStockExchangeController.onPageLoad(mode).url),
        bound.yesNoQuestion(SharePortfolioOnStockExchangePage, "shares.portfolioOnStockExchangeYesNo", SharePortfolioOnStockExchangeController.onPageLoad(mode).url),
        bound.shareClassQuestion(ShareClassPage, "shares.class", ShareClassController.onPageLoad(mode).url),
        bound.numberQuestion(ShareQuantityInTrustPage, "shares.quantityInTrust", ShareQuantityInTrustController.onPageLoad(mode).url),
        bound.numberQuestion(SharePortfolioQuantityInTrustPage, "shares.portfolioQuantityInTrust", SharePortfolioQuantityInTrustController.onPageLoad(mode).url),
        bound.currencyQuestion(ShareValueInTrustPage, "shares.valueInTrust", ShareValueInTrustController.onPageLoad(mode).url),
        bound.currencyQuestion(SharePortfolioValueInTrustPage, "shares.portfolioValueInTrust", SharePortfolioValueInTrustController.onPageLoad(mode).url)
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)

  }

}
