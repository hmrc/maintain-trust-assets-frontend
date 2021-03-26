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
import controllers.asset.routes.WhatKindOfAssetController
import controllers.routes.SessionExpiredController
import javax.inject.Inject
import models.WhatKindOfAsset.{Business, Money, NonEeaBusiness, Other, Partnership, PropertyOrLand, Shares}
import models.{AddAssets, Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.{AddAnAssetYesNoPage, AddAssetsPage, AssetInterruptPage, WhatKindOfAssetPage}
import play.api.mvc.Call

class AssetsNavigator @Inject()(config: FrontendAppConfig) extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  override def nextPage(page: Page, userAnswers: UserAnswers): Call =
    nextPage(page, NormalMode, userAnswers)

  def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case AssetInterruptPage => ua => routeToAssetIndex(ua)
    case WhatKindOfAssetPage => ua => whatKindOfAssetRoute(ua)
    case AddAssetsPage => ua => addAssetsRoute()(ua)
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

  private def addAssetsRoute()(answers: UserAnswers): Call = {
    val addAnother = answers.get(AddAssetsPage)

    addAnother match {
      case Some(AddAssets.YesNow) =>
        routeToAssetIndex(answers)
      case Some(AddAssets.YesLater) =>
        assetsCompletedRoute()
      case Some(AddAssets.NoComplete) =>
        assetsCompletedRoute()
      case _ => SessionExpiredController.onPageLoad()
    }
  }

  def assetsCompletedRoute() : Call = {
    Call("GET", config.registrationProgressUrl())
  }

  private def routeToAssetIndex(answers: UserAnswers): Call = {
    val assets = answers.get(sections.Assets).getOrElse(List.empty)

    if (answers.isTaxable) {
      val index = assets.size
      WhatKindOfAssetController.onPageLoad()
    } else {
      // assets includes an in progress non-EEA business asset as we have just set the value in WhatKindOfAssetPage
      // therefore we need the index to correspond to that asset (i.e. assets.size - 1)
      val index = assets.size - 1
      controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode)
    }
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case AddAnAssetYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = AddAnAssetYesNoPage,
      yesCall = routeToAssetIndex(ua),
      noCall = assetsCompletedRoute()
    )
  }

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
  simpleNavigation(mode) orElse
    yesNoNavigation(mode)

}