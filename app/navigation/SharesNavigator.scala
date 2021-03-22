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

import config.FrontendAppConfig
import controllers.asset.routes._
import controllers.asset.shares.routes._
import models.UserAnswers
import pages.Page
import pages.asset.shares._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

import javax.inject.{Inject, Singleton}

@Singleton
class SharesNavigator @Inject()(config: FrontendAppConfig) extends Navigator(config) {

  override protected def route(): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    portfolioRoutes() orElse
    nonPortfolioRoutes() orElse {
      case page @ SharesInAPortfolioPage(index) => _ => ua => yesNoNav(
        ua = ua,
        fromPage = page,
        yesCall = SharePortfolioNameController.onPageLoad(index),
        noCall = ShareCompanyNameController.onPageLoad(index)
      )
      case ShareAnswerPage => _ => _ => AddAssetsController.onPageLoad()
    }
  }

  private def portfolioRoutes(): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case SharePortfolioNamePage(index) => _ => _ => SharePortfolioOnStockExchangeController.onPageLoad(index)
    case SharePortfolioOnStockExchangePage(index) => _ => _ => SharePortfolioQuantityInTrustController.onPageLoad(index)
    case SharePortfolioQuantityInTrustPage(index) => _ => _ => SharePortfolioValueInTrustController.onPageLoad(index)
    case SharePortfolioValueInTrustPage(index) => _ => _ => ShareAnswerController.onPageLoad(index)
  }

  private def nonPortfolioRoutes(): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case ShareCompanyNamePage(index) => _ => _ => SharesOnStockExchangeController.onPageLoad(index)
    case SharesOnStockExchangePage(index) => _ => _ => ShareClassController.onPageLoad(index)
    case ShareClassPage(index) => _ => _ => ShareQuantityInTrustController.onPageLoad(index)
    case ShareQuantityInTrustPage(index) => _ => _ => ShareValueInTrustController.onPageLoad(index)
    case ShareValueInTrustPage(index) => _ => _ => ShareAnswerController.onPageLoad(index)
  }

}
