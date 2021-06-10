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
import controllers.asset.routes.AssetInterruptPageController
import controllers.routes.SessionExpiredController

import javax.inject.Inject
import models.WhatKindOfAsset.{Business, Money, NonEeaBusiness, Other, Partnership, PropertyOrLand, Shares}
import models.assets.Assets
import models.{AddAssets, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.nontaxabletotaxable.AddAssetsYesNoPage
import pages.asset.{AddAnAssetYesNoPage, AddAssetsPage, AssetInterruptPage, TrustOwnsNonEeaBusinessYesNoPage, WhatKindOfAssetPage}
import play.api.Logging
import play.api.mvc.Call

class AssetsNavigator @Inject()(config: FrontendAppConfig) extends Navigator with Logging {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode, Assets())(page)(userAnswers)

  override def nextPage(page: Page, userAnswers: UserAnswers, assets: Assets = Assets()): Call =
    routes(NormalMode, assets)(page)(userAnswers)

  def simpleNavigation(mode: Mode, assets: Assets): PartialFunction[Page, UserAnswers => Call] = {
    case AssetInterruptPage => ua => routeToAssetIndex(ua, assets)
    case WhatKindOfAssetPage => ua => whatKindOfAssetRoute(ua)
    case AddAssetsPage => ua => addAssetsRoute()(ua, assets)
  }

  private def whatKindOfAssetRoute(answers: UserAnswers): Call =
    answers.get(WhatKindOfAssetPage) match {
      case Some(Money) =>
        controllers.asset.money.routes.AssetMoneyValueController.onPageLoad(NormalMode)
      case Some(PropertyOrLand) =>
        controllers.asset.property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode)
      case Some(Shares) =>
        controllers.asset.shares.routes.SharesInAPortfolioController.onPageLoad(NormalMode)
      case Some(Business) =>
        controllers.asset.business.routes.BusinessNameController.onPageLoad(NormalMode)
      case Some(Partnership) =>
        controllers.asset.partnership.routes.PartnershipDescriptionController.onPageLoad(NormalMode)
      case Some(Other) =>
        controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode)
      case Some(NonEeaBusiness) =>
        controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode)
      case _ =>
        SessionExpiredController.onPageLoad()
    }

  private def addAssetsRoute()(answers: UserAnswers, assets: Assets): Call = {
    answers.get(AddAssetsPage) match {
      case Some(AddAssets.YesNow) =>
        routeToAssetIndex(answers, assets)
      case Some(AddAssets.NoComplete) =>
        nonEEAAssetCompletedRoute()
      case _ => SessionExpiredController.onPageLoad()
    }
  }

  def nonEEAAssetCompletedRoute() : Call =
    controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.submitComplete()

  private def routeToAssetIndex(answers: UserAnswers, assets: Assets): Call = {
    (answers.isMigratingToTaxable, assets)  match {
      case (true, x) if x.isEmpty => controllers.asset.routes.WhatKindOfAssetController.onPageLoad()
      case (true, _)  => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
      case (false, _) => controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode)
    }
  }

  private def yesNoNavigation(mode: Mode, assets: Assets): PartialFunction[Page, UserAnswers => Call] = {
    case AddAssetsYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = AddAssetsYesNoPage,
      yesCall = AssetInterruptPageController.onPageLoad(),
      noCall = noRouteToMaintenance(assets)
    )
    case AddAnAssetYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = AddAnAssetYesNoPage,
      yesCall = routeToAssetIndex(ua, assets),
      noCall = nonEEAAssetCompletedRoute()
    )
    case TrustOwnsNonEeaBusinessYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = TrustOwnsNonEeaBusinessYesNoPage,
      yesCall = AssetInterruptPageController.onPageLoad(),
      noCall = nonEEAAssetCompletedRoute()
    )
  }

  private def noRouteToMaintenance(assets: Assets): Call =
    if (assets.isEmpty) {
      assetsInProgressHubRoute()
    } else {
      controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
    }

  def assetsInProgressHubRoute() : Call = {
    Call("GET", config.maintainATrustOverview)
  }

  def routes(mode: Mode, assets: Assets): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode, assets) orElse
      yesNoNavigation(mode, assets)

}