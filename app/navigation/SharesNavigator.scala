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

package navigation

import config.FrontendAppConfig
import models.UserAnswers
import navigation.AssetsRoutes.assetsCompletedRoute
import pages.Page
import pages.asset.shares._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

import javax.inject.{Inject, Singleton}

@Singleton
class SharesNavigator @Inject()(config: FrontendAppConfig) extends Navigator(config) {

  override protected def route(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    portfolioRoutes(draftId) orElse
    nonPortfolioRoutes(draftId) orElse {
      case SharesInAPortfolioPage(index) => _ =>
        ua =>
          sharesInAPortfolio(ua, index, draftId, config)
      case ShareAnswerPage => _ =>
        _ =>
          controllers.asset.routes.AddAssetsController.onPageLoad(draftId)
    }
  }

  private def portfolioRoutes(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case SharePortfolioNamePage(index) => _ => _ =>
      controllers.asset.shares.routes.SharePortfolioOnStockExchangeController.onPageLoad(index, draftId)
    case SharePortfolioOnStockExchangePage(index) => _ => _ =>
      controllers.asset.shares.routes.SharePortfolioQuantityInTrustController.onPageLoad(index, draftId)
    case SharePortfolioQuantityInTrustPage(index) => _ => _ =>
      controllers.asset.shares.routes.SharePortfolioValueInTrustController.onPageLoad(index, draftId)
    case SharePortfolioValueInTrustPage(index) => _ => _ =>
      controllers.asset.shares.routes.ShareAnswerController.onPageLoad(index, draftId)
  }

  private def nonPortfolioRoutes(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case ShareCompanyNamePage(index) => _ => _ =>
      controllers.asset.shares.routes.SharesOnStockExchangeController.onPageLoad(index, draftId)
    case SharesOnStockExchangePage(index) => _ => _ =>
      controllers.asset.shares.routes.ShareClassController.onPageLoad(index, draftId)
    case ShareClassPage(index) => _ => _ =>
      controllers.asset.shares.routes.ShareQuantityInTrustController.onPageLoad(index, draftId)
    case ShareQuantityInTrustPage(index) => _ => _ =>
      controllers.asset.shares.routes.ShareValueInTrustController.onPageLoad(index, draftId)
    case ShareValueInTrustPage(index) => _ => _ =>
      controllers.asset.shares.routes.ShareAnswerController.onPageLoad(index, draftId)
  }

  private def sharesInAPortfolio(userAnswers: UserAnswers, index : Int, draftId: String, config: FrontendAppConfig) : Call = {
    userAnswers.get(SharesInAPortfolioPage(index)) match {
      case Some(true) =>
        controllers.asset.shares.routes.SharePortfolioNameController.onPageLoad(index, draftId)
      case Some(false) =>
        controllers.asset.shares.routes.ShareCompanyNameController.onPageLoad(index, draftId)
      case _=> assetsCompletedRoute(draftId, config)
    }
  }

}
