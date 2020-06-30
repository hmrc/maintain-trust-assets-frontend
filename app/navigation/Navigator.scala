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
import javax.inject.{Inject, Singleton}
import models.WhatKindOfAsset._
import models.{UserAnswers, _}
import pages.Page
import pages.asset._
import pages.asset.money._
import pages.asset.other.{OtherAssetDescriptionPage, OtherAssetValuePage}
import pages.asset.shares._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

object AssetsRoutes {
  def route(draftId: String, config: FrontendAppConfig): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case AssetMoneyValuePage(index) => _ => ua => assetMoneyValueRoute(ua, index, draftId)
    case WhatKindOfAssetPage(index) => _ => ua => whatKindOfAssetRoute(ua, index, draftId)
    case SharesInAPortfolioPage(index) => _ => ua => sharesInAPortfolio(ua, index, draftId, config)
    case SharePortfolioNamePage(index) => _ => ua => controllers.asset.shares.routes.SharePortfolioOnStockExchangeController.onPageLoad(NormalMode, index, draftId)
    case SharePortfolioOnStockExchangePage(index) => _ => ua => controllers.asset.shares.routes.SharePortfolioQuantityInTrustController.onPageLoad(NormalMode, index, draftId)
    case SharePortfolioQuantityInTrustPage(index) => _ => _ => controllers.asset.shares.routes.SharePortfolioValueInTrustController.onPageLoad(NormalMode, index, draftId)
    case SharePortfolioValueInTrustPage(index) => _ => _ => controllers.asset.shares.routes.ShareAnswerController.onPageLoad(index, draftId)
    case SharesOnStockExchangePage(index) => _ => _ => controllers.asset.shares.routes.ShareClassController.onPageLoad(NormalMode, index, draftId)
    case ShareClassPage(index) => _ => _ => controllers.asset.shares.routes.ShareQuantityInTrustController.onPageLoad(NormalMode, index, draftId)
    case AddAssetsPage => _ => addAssetsRoute(draftId, config)
    case AddAnAssetYesNoPage => _ => addAnAssetYesNoRoute(draftId, config)
    case ShareQuantityInTrustPage(index) => _ => _ => controllers.asset.shares.routes.ShareValueInTrustController.onPageLoad(NormalMode, index, draftId)
    case ShareValueInTrustPage(index) => _ => _ => controllers.asset.shares.routes.ShareAnswerController.onPageLoad(index, draftId)
    case ShareAnswerPage => _ => _ => controllers.asset.routes.AddAssetsController.onPageLoad(draftId)
    case ShareCompanyNamePage(index) => _ => _ => controllers.asset.shares.routes.SharesOnStockExchangeController.onPageLoad(NormalMode, index, draftId)
    case OtherAssetDescriptionPage(index) => _ => _ => controllers.asset.other.routes.OtherAssetValueController.onPageLoad(NormalMode, index, draftId)
    case OtherAssetValuePage(index) => _ => _ => controllers.asset.other.routes.OtherAssetAnswersController.onPageLoad(index, draftId)
  }

  private def assetsCompletedRoute(draftId: String, config: FrontendAppConfig) : Call = {
    Call("GET", config.registrationProgressUrl(draftId))
  }

  private def sharesInAPortfolio(userAnswers: UserAnswers, index : Int, draftId: String, config: FrontendAppConfig) : Call = {
    userAnswers.get(SharesInAPortfolioPage(index)) match {
      case Some(true) =>
        controllers.asset.shares.routes.SharePortfolioNameController.onPageLoad(NormalMode, index, draftId)
      case Some(false) =>
        controllers.asset.shares.routes.ShareCompanyNameController.onPageLoad(NormalMode, index, draftId)
      case _=> assetsCompletedRoute(draftId, config)
    }
  }

  private def addAnAssetYesNoRoute(draftId: String, config: FrontendAppConfig)(userAnswers: UserAnswers) : Call = userAnswers.get(AddAnAssetYesNoPage) match {
    case Some(false) => assetsCompletedRoute(draftId, config)
    case Some(true) => controllers.routes.WhatKindOfAssetController.onPageLoad(NormalMode, 0, draftId)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def addAssetsRoute(draftId: String, config: FrontendAppConfig)(answers: UserAnswers) = {
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
        assetsCompletedRoute(draftId, config)
      case Some(AddAssets.NoComplete) =>
        assetsCompletedRoute(draftId, config)
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
        controllers.asset.partnership.routes.PartnershipDescriptionController.onPageLoad(NormalMode, index, draftId)
      case Some(Other) =>
        controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode, index, draftId)
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
      AssetsRoutes.route(draftId, config) orElse
      defaultRoute(draftId)

  def nextPage(page: Page, mode: Mode, draftId: String, af :AffinityGroup = AffinityGroup.Organisation): UserAnswers => Call = mode match {
    case NormalMode =>
      route(draftId)(page)(af)
    case CheckMode =>
      route(draftId)(page)(af)
  }
}
