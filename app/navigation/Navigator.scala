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
import controllers.asset.routes
import javax.inject.{Inject, Singleton}
import models.WhatKindOfAsset._
import models.{UserAnswers, _}
import pages.Page
import pages.asset._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

object AssetsRoutes {

  def route(draftId: String, config: FrontendAppConfig): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case WhatKindOfAssetPage(index) => _ => ua => whatKindOfAssetRoute(ua, index, draftId)
    case AddAssetsPage => _ => addAssetsRoute(draftId, config)
    case AddAnAssetYesNoPage => _ => addAnAssetYesNoRoute(draftId, config)
  }

  def assetsCompletedRoute(draftId: String, config: FrontendAppConfig) : Call = {
    Call("GET", config.registrationProgressUrl(draftId))
  }

  private def addAnAssetYesNoRoute(draftId: String, config: FrontendAppConfig)(userAnswers: UserAnswers): Call = userAnswers.get(AddAnAssetYesNoPage) match {
    case Some(false) => assetsCompletedRoute(draftId, config)
    case Some(true) => routes.WhatKindOfAssetController.onPageLoad(0, draftId)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def addAssetsRoute(draftId: String, config: FrontendAppConfig)(answers: UserAnswers): Call = {
    val addAnother = answers.get(AddAssetsPage)

    def routeToAssetIndex: Call = {
      val assets = answers.get(sections.Assets).getOrElse(List.empty)
      routes.WhatKindOfAssetController.onPageLoad(assets.size, draftId)
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

  private def whatKindOfAssetRoute(answers: UserAnswers, index: Int, draftId: String): Call =
    answers.get(WhatKindOfAssetPage(index)) match {
      case Some(Money) =>
        controllers.asset.money.routes.AssetMoneyValueController.onPageLoad(index, draftId)
      case Some(PropertyOrLand) =>
        controllers.asset.property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(index, draftId)
      case Some(Shares) =>
        controllers.asset.shares.routes.SharesInAPortfolioController.onPageLoad(index, draftId)
      case Some(Business) =>
        controllers.asset.business.routes.BusinessNameController.onPageLoad(index, draftId)
      case Some(Partnership) =>
        controllers.asset.partnership.routes.PartnershipDescriptionController.onPageLoad(index, draftId)
      case Some(Other) =>
        controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad(index, draftId)
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
}

@Singleton
class Navigator @Inject()(config: FrontendAppConfig) {

  private def defaultRoute(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case _ => _ => _ => controllers.routes.IndexController.onPageLoad(draftId)
  }

  protected def route(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] =
      AssetsRoutes.route(draftId, config) orElse
      defaultRoute(draftId)

  def nextPage(page: Page, draftId: String, af :AffinityGroup = AffinityGroup.Organisation): UserAnswers => Call = {
    route(draftId)(page)(af)
  }
}
