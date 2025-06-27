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

package navigation

import controllers.asset.shares.routes._
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.shares._
import pages.asset.shares.add.ShareAnswerPage
import pages.asset.shares.amend.IndexPage
import play.api.mvc.Call

import javax.inject.Inject

class SharesNavigator @Inject()() extends Navigator() {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation orElse
      portfolioRoutes(mode) orElse
      nonPortfolioRoutes(mode) orElse
      yesNoNavigation(mode)

  def simpleNavigation: PartialFunction[Page, UserAnswers => Call] = {
    case ShareAnswerPage(index) => _ => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
  }

  private def portfolioRoutes(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case SharePortfolioNamePage(index)  => _ => SharePortfolioOnStockExchangeController.onPageLoad(index, mode)
    case SharePortfolioOnStockExchangePage(index) => _ => SharePortfolioQuantityInTrustController.onPageLoad(index, mode)
    case SharePortfolioQuantityInTrustPage(index) => _ => SharePortfolioValueInTrustController.onPageLoad(index, mode)
    case SharePortfolioValueInTrustPage(index) => ua => navigateToCheckAnswers(ua, mode, index)
  }

  private def nonPortfolioRoutes(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case ShareCompanyNamePage(index) => _ => SharesOnStockExchangeController.onPageLoad(index, mode)
    case SharesOnStockExchangePage(index) => _ => ShareClassController.onPageLoad(index, mode)
    case ShareClassPage(index) => _ => ShareQuantityInTrustController.onPageLoad(index, mode)
    case ShareQuantityInTrustPage(index)  => _ => ShareValueInTrustController.onPageLoad(index, mode)
    case ShareValueInTrustPage(index) => ua => navigateToCheckAnswers(ua, mode, index)
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case SharesInAPortfolioPage(index) => ua => yesNoNav(
      ua = ua,
      fromPage = SharesInAPortfolioPage(index),
      yesCall = SharePortfolioNameController.onPageLoad(index, mode),
      noCall = ShareCompanyNameController.onPageLoad(index, mode)
    )
  }

  private def navigateToCheckAnswers(ua: UserAnswers, mode: Mode, index: Int): Call = {
    if (mode == NormalMode) {
      controllers.asset.shares.add.routes.ShareAnswerController.onPageLoad(index)
    } else {
      ua.get(IndexPage) match {
        case Some(index) => controllers.asset.shares.amend.routes.ShareAmendAnswersController.renderFromUserAnswers(index)
        case None => controllers.routes.SessionExpiredController.onPageLoad
      }
    }
  }
}
