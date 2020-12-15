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

import controllers.asset.shares.routes._
import models.{NormalMode, UserAnswers}
import pages.asset.shares._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import utils.{AnswerRowConverter, CheckAnswersFormatters}
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class SharesPrintHelper @Inject()(countryOptions: CountryOptions,
                                  checkAnswersFormatters: CheckAnswersFormatters) extends PrintHelper {

  def printSection(userAnswers: UserAnswers,
                   arg: String = "",
                   index: Int,
                   draftId: String)
                  (implicit messages: Messages): AnswerSection = {

    section(userAnswers, arg, index, draftId, Some(messages("answerPage.section.shareAsset.subheading", index + 1)))
  }

  override def answerRows(userAnswers: UserAnswers,
                          arg: String = "",
                          index: Int,
                          draftId: String)
                         (implicit messages: Messages): Seq[AnswerRow] = {

    val converter: AnswerRowConverter = new AnswerRowConverter(countryOptions, checkAnswersFormatters)(userAnswers, arg)

    Seq(
      converter.assetTypeQuestion(index, draftId),
      converter.yesNoQuestion(SharesInAPortfolioPage(index), "shares.inAPortfolioYesNo", SharesInAPortfolioController.onPageLoad(NormalMode, index, draftId).url),
      converter.stringQuestion(ShareCompanyNamePage(index), "shares.companyName", ShareCompanyNameController.onPageLoad(NormalMode, index, draftId).url),
      converter.stringQuestion(SharePortfolioNamePage(index), "shares.portfolioName", SharePortfolioNameController.onPageLoad(NormalMode, index, draftId).url),
      converter.yesNoQuestion(SharesOnStockExchangePage(index), "shares.onStockExchangeYesNo", SharesOnStockExchangeController.onPageLoad(NormalMode, index, draftId).url),
      converter.yesNoQuestion(SharePortfolioOnStockExchangePage(index), "shares.portfolioOnStockExchangeYesNo", SharePortfolioOnStockExchangeController.onPageLoad(NormalMode, index, draftId).url),
      converter.shareClassQuestion(ShareClassPage(index), "shares.class", ShareClassController.onPageLoad(NormalMode, index, draftId).url),
      converter.stringQuestion(ShareQuantityInTrustPage(index), "shares.quantityInTrust", ShareQuantityInTrustController.onPageLoad(NormalMode, index, draftId).url),
      converter.stringQuestion(SharePortfolioQuantityInTrustPage(index), "shares.portfolioQuantityInTrust", SharePortfolioQuantityInTrustController.onPageLoad(NormalMode, index, draftId).url),
      converter.currencyQuestion(ShareValueInTrustPage(index), "shares.valueInTrust", ShareValueInTrustController.onPageLoad(NormalMode, index, draftId).url),
      converter.currencyQuestion(SharePortfolioValueInTrustPage(index), "shares.portfolioValueInTrust", SharePortfolioValueInTrustController.onPageLoad(NormalMode, index, draftId).url)
    ).flatten
  }
}
