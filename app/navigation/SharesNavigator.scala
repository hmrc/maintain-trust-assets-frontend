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

import controllers.asset.routes._
import controllers.asset.shares.routes._
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.shares._
import play.api.mvc.Call
import javax.inject.Inject

class SharesNavigator @Inject()() extends Navigator() {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  override def nextPage(page: Page, userAnswers: UserAnswers): Call =
    nextPage(page, NormalMode, userAnswers)

  def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case ShareAnswerPage => _ => AddAssetsController.onPageLoad()
  }

  private def portfolioRoutes(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case SharePortfolioNamePage  => _ => SharePortfolioOnStockExchangeController.onPageLoad(mode)
    case SharePortfolioOnStockExchangePage => _ => SharePortfolioQuantityInTrustController.onPageLoad(mode)
    case SharePortfolioQuantityInTrustPage => _ => SharePortfolioValueInTrustController.onPageLoad(mode)
    case SharePortfolioValueInTrustPage => _ => ShareAnswerController.onPageLoad()
  }

  private def nonPortfolioRoutes(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case ShareCompanyNamePage => _ => SharesOnStockExchangeController.onPageLoad(mode)
    case SharesOnStockExchangePage => _ => ShareClassController.onPageLoad(mode)
    case ShareClassPage => _ => ShareQuantityInTrustController.onPageLoad(mode)
    case ShareQuantityInTrustPage  => _ => ShareValueInTrustController.onPageLoad(mode)
    case ShareValueInTrustPage => _ => ShareAnswerController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case page @ SharesInAPortfolioPage => ua => yesNoNav(
      ua = ua,
      fromPage = page,
      yesCall = SharePortfolioNameController.onPageLoad(mode),
      noCall = ShareCompanyNameController.onPageLoad(mode)
    )
  }

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) orElse
    portfolioRoutes(mode) orElse
    nonPortfolioRoutes(mode) orElse
      yesNoNavigation(mode)

}
