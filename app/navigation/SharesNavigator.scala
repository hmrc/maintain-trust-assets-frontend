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

package navigation

import controllers.asset.shares.routes._
import models.assets.Assets
import javax.inject.Inject
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.shares.amend.IndexPage
import pages.asset.shares._
import pages.asset.shares.add.ShareAnswerPage
import play.api.mvc.Call

class SharesNavigator @Inject()() extends Navigator() {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  override def nextPage(page: Page, userAnswers: UserAnswers, assets: Assets = Assets()): Call =
    nextPage(page, NormalMode, userAnswers)

  def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case ShareAnswerPage => _ => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
  }

  private def portfolioRoutes(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case SharePortfolioNamePage  => _ => SharePortfolioOnStockExchangeController.onPageLoad(mode)
    case SharePortfolioOnStockExchangePage => _ => SharePortfolioQuantityInTrustController.onPageLoad(mode)
    case SharePortfolioQuantityInTrustPage => _ => SharePortfolioValueInTrustController.onPageLoad(mode)
    case SharePortfolioValueInTrustPage => ua => navigateToCheckAnswers(ua, mode)
  }

  private def nonPortfolioRoutes(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case ShareCompanyNamePage => _ => SharesOnStockExchangeController.onPageLoad(mode)
    case SharesOnStockExchangePage => _ => ShareClassController.onPageLoad(mode)
    case ShareClassPage => _ => ShareQuantityInTrustController.onPageLoad(mode)
    case ShareQuantityInTrustPage  => _ => ShareValueInTrustController.onPageLoad(mode)
    case ShareValueInTrustPage => ua => navigateToCheckAnswers(ua, mode)
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case SharesInAPortfolioPage => ua => yesNoNav(
      ua = ua,
      fromPage = SharesInAPortfolioPage,
      yesCall = SharePortfolioNameController.onPageLoad(mode),
      noCall = ShareCompanyNameController.onPageLoad(mode)
    )
  }

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) orElse
    portfolioRoutes(mode) orElse
    nonPortfolioRoutes(mode) orElse
      yesNoNavigation(mode)

  private def navigateToCheckAnswers(ua: UserAnswers, mode: Mode): Call = {
    if (mode == NormalMode) {
      controllers.asset.shares.add.routes.ShareAnswerController.onPageLoad()
    } else {
      ua.get(IndexPage) match {
        case Some(index) => controllers.asset.shares.amend.routes.ShareAmendAnswersController.renderFromUserAnswers(index)
        case None => controllers.routes.SessionExpiredController.onPageLoad()
      }
    }
  }
}
