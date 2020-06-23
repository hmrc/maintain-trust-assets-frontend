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
import controllers.asset.routes
import javax.inject.{Inject, Singleton}
import models._
import models.UserAnswers
import models.WhatKindOfAsset._
import pages.Page
import pages.asset._
import pages.asset.money._
import pages.asset.shares._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

object AssetsRoutes {
  def route(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case AssetMoneyValuePage(index) => _ => ua => assetMoneyValueRoute(ua, index, draftId)
    case WhatKindOfAssetPage(index) => _ => ua => whatKindOfAssetRoute(ua, index, draftId)
    case SharesInAPortfolioPage(index) => _ => ua => sharesInAPortfolio(ua, index, draftId)
    case SharePortfolioNamePage(index) => _ => ua => controllers.asset.shares.routes.SharePortfolioOnStockExchangeController.onPageLoad(NormalMode, index, draftId)
    case SharePortfolioOnStockExchangePage(index) => _ => ua => controllers.asset.shares.routes.SharePortfolioQuantityInTrustController.onPageLoad(NormalMode, index, draftId)
    case SharePortfolioQuantityInTrustPage(index) => _ => _ => controllers.asset.shares.routes.SharePortfolioValueInTrustController.onPageLoad(NormalMode, index, draftId)
    case SharePortfolioValueInTrustPage(index) => _ => _ => controllers.asset.shares.routes.ShareAnswerController.onPageLoad(index, draftId)
    case SharesOnStockExchangePage(index) => _ => _ => controllers.asset.shares.routes.ShareClassController.onPageLoad(NormalMode, index, draftId)
    case ShareClassPage(index) => _ => _ => controllers.asset.shares.routes.ShareQuantityInTrustController.onPageLoad(NormalMode, index, draftId)
    case AddAssetsPage => _ => addAssetsRoute(draftId)
    case AddAnAssetYesNoPage => _ => addAnAssetYesNoRoute(draftId)
    case ShareQuantityInTrustPage(index) => _ => _ => controllers.asset.shares.routes.ShareValueInTrustController.onPageLoad(NormalMode, index, draftId)
    case ShareValueInTrustPage(index) => _ => _ => controllers.asset.shares.routes.ShareAnswerController.onPageLoad(index, draftId)
    case ShareAnswerPage => _ => _ => controllers.asset.routes.AddAssetsController.onPageLoad(draftId)
    case ShareCompanyNamePage(index) => _ => _ => controllers.asset.shares.routes.SharesOnStockExchangeController.onPageLoad(NormalMode, index, draftId)
  }

  private def sharesInAPortfolio(userAnswers: UserAnswers, index : Int, draftId: String) : Call = {
    userAnswers.get(SharesInAPortfolioPage(index)) match {
      case Some(true) =>
        controllers.asset.shares.routes.SharePortfolioNameController.onPageLoad(NormalMode, index, draftId)
      case Some(false) =>
        controllers.asset.shares.routes.ShareCompanyNameController.onPageLoad(NormalMode, index, draftId)
      case _=>
        routes.AssetsCompleteController.onPageLoad(draftId)
    }
  }

  private def addAnAssetYesNoRoute(draftId: String)(userAnswers: UserAnswers) : Call = userAnswers.get(AddAnAssetYesNoPage) match {
    case Some(false) => routes.AssetsCompleteController.onPageLoad(draftId)
    case Some(true) => controllers.routes.WhatKindOfAssetController.onPageLoad(NormalMode, 0, draftId)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def addAssetsRoute(draftId: String)(answers: UserAnswers) = {
    val addAnother = answers.get(AddAssetsPage)

    def routeToAssetIndex = {
      val assets = answers.get(sections.Assets).getOrElse(List.empty)
      assets match {
        case Nil =>
          controllers.routes.WhatKindOfAssetController.onPageLoad(NormalMode, 0, draftId)
        case t if t.nonEmpty =>
          controllers.routes.WhatKindOfAssetController.onPageLoad(NormalMode, t.size, draftId)
      }
    }

    addAnother match {
      case Some(AddAssets.YesNow) =>
        routeToAssetIndex
      case Some(AddAssets.YesLater) =>
        routes.AssetsCompleteController.onPageLoad(draftId)
      case Some(AddAssets.NoComplete) =>
        routes.AssetsCompleteController.onPageLoad(draftId)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def assetMoneyValueRoute(answers: UserAnswers, index: Int, draftId: String) = {
    val assets = answers.get(sections.Assets).getOrElse(List.empty)
    assets match  {
      case Nil => controllers.routes.WhatKindOfAssetController.onPageLoad(NormalMode, 0, draftId)
      case _ => controllers.asset.routes.AddAssetsController.onPageLoad(draftId)
    }
  }

  private def whatKindOfAssetRoute(answers: UserAnswers, index: Int, draftId: String) =
    answers.get(WhatKindOfAssetPage(index)) match {
      case Some(Money) =>
        controllers.asset.money.routes.AssetMoneyValueController.onPageLoad(NormalMode, index, draftId)
      case Some(Shares) =>
        controllers.asset.shares.routes.SharesInAPortfolioController.onPageLoad(NormalMode, index, draftId)
      case Some(PropertyOrLand) =>
        controllers.asset.property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode, index, draftId)
      case Some(Business) =>
        controllers.routes.FeatureNotAvailableController.onPageLoad()
      case Some(Partnership) =>
        controllers.routes.FeatureNotAvailableController.onPageLoad()
      case Some(Other) =>
        controllers.routes.FeatureNotAvailableController.onPageLoad()
      case _ =>
        controllers.routes.FeatureNotAvailableController.onPageLoad()
    }
}

@Singleton
class Navigator @Inject()(
                           config: FrontendAppConfig
                         ) {

  private def defaultRoute(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case _ => _ => _ => controllers.routes.IndexController.onPageLoad(draftId)
  }

  protected def route(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] =
      AssetsRoutes.route(draftId) orElse
      defaultRoute(draftId)

  def nextPage(page: Page, mode: Mode, draftId: String, af :AffinityGroup = AffinityGroup.Organisation): UserAnswers => Call = mode match {
    case NormalMode =>
      route(draftId)(page)(af)
    case CheckMode =>
      route(draftId)(page)(af)
  }
}
