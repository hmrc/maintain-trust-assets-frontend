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

import controllers.asset.shares.routes._
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.asset.shares._
import play.api.i18n.Messages
import utils.AnswerRowConverter
import viewmodels.{AnswerRow, AnswerSection}
import javax.inject.Inject

class SharesPrintHelper @Inject() (answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, index: Int, provisional: Boolean, name: String)(implicit
    messages: Messages
  ): AnswerSection = {

    val bound: answerRowConverter.Bound = answerRowConverter.bind(userAnswers, name)

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (provisional) NormalMode else CheckMode
      Seq(
        bound.assetTypeQuestion(index),
        bound.yesNoQuestion(
          SharesInAPortfolioPage(index),
          "shares.inAPortfolioYesNo",
          SharesInAPortfolioController.onPageLoad(index, mode).url
        ),
        bound.stringQuestion(
          ShareCompanyNamePage(index),
          "shares.companyName",
          ShareCompanyNameController.onPageLoad(index, mode).url
        ),
        bound.stringQuestion(
          SharePortfolioNamePage(index),
          "shares.portfolioName",
          SharePortfolioNameController.onPageLoad(index, mode).url
        ),
        bound.yesNoQuestion(
          SharesOnStockExchangePage(index),
          "shares.onStockExchangeYesNo",
          SharesOnStockExchangeController.onPageLoad(index, mode).url
        ),
        bound.yesNoQuestion(
          SharePortfolioOnStockExchangePage(index),
          "shares.portfolioOnStockExchangeYesNo",
          SharePortfolioOnStockExchangeController.onPageLoad(index, mode).url
        ),
        bound.shareClassQuestion(
          ShareClassPage(index),
          "shares.class",
          ShareClassController.onPageLoad(index, mode).url
        ),
        bound.numberQuestion(
          ShareQuantityInTrustPage(index),
          "shares.quantityInTrust",
          ShareQuantityInTrustController.onPageLoad(index, mode).url
        ),
        bound.numberQuestion(
          SharePortfolioQuantityInTrustPage(index),
          "shares.portfolioQuantityInTrust",
          SharePortfolioQuantityInTrustController.onPageLoad(index, mode).url
        ),
        bound.currencyQuestion(
          ShareValueInTrustPage(index),
          "shares.valueInTrust",
          ShareValueInTrustController.onPageLoad(index, mode).url
        ),
        bound.currencyQuestion(
          SharePortfolioValueInTrustPage(index),
          "shares.portfolioValueInTrust",
          SharePortfolioValueInTrustController.onPageLoad(index, mode).url
        )
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)

  }

}
