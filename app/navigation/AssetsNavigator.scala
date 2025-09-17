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

import config.FrontendAppConfig
import controllers.asset.routes.AssetInterruptPageController
import models.Constants._
import models.WhatKindOfAsset.{Business, Money, NonEeaBusiness, Other, Partnership, PropertyOrLand, Shares}
import models.assets.{AssetType, Assets}
import models.{Mode, NormalMode, WhatKindOfAsset}
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpVerbs.GET

import javax.inject.Inject

class AssetsNavigator @Inject()(config: FrontendAppConfig) {

  def redirectToAddAssetPage(isMigratingToTaxable: Boolean, index: Option[Int] = None): Call = {
    if (isMigratingToTaxable) {
      controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
    } else {
      controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad(if(index.isDefined)  index.get else 0)
    }
  }

  def   redirectFromInterruptPage(isMigratingToTaxable: Boolean, noAssets: Boolean): Call = {
    (isMigratingToTaxable, noAssets) match {
      case (true, true) =>
        AssetNavigator.routeToIndex(
          List.empty,
          controllers.asset.routes.WhatKindOfAssetController.onPageLoad
        )
      case (true, false) => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
      case (false, _) => controllers.asset.noneeabusiness.routes.NameController.onPageLoad(0,NormalMode)
    }
  }

  def redirectFromAddAssetYesNoPage(value: Boolean, isMigratingToTaxable: Boolean, noAssets: Boolean): Call = {
    (value, isMigratingToTaxable, noAssets) match {
      case (true, true, _) => AssetInterruptPageController.onPageLoad()
      case (true, false, _) => controllers.asset.noneeabusiness.routes.NameController.onPageLoad(0,NormalMode)
      case (false, true, true) => maintainATrustOverview
      case (false, true, false) => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
      case (false, false, _) => submitTaskComplete(isMigratingToTaxable)
    }
  }

  def redirectFromEntryQuestion(value: Boolean, isMigratingToTaxable: Boolean): Call = {
    if (value) {
      AssetInterruptPageController.onPageLoad()
    } else {
      submitTaskComplete(isMigratingToTaxable)
    }
  }

  def addAssetRoute(assets: Assets, index: Int): Call = {
    case class AssetRoute(size: Int, maxSize: Int, route: Call)

    val routes: List[AssetRoute] = List(
      AssetRoute(assets.monetary.size, MAX_MONEY_ASSETS, addAssetNowRoute(Money, assets.monetary)),
      AssetRoute(assets.propertyOrLand.size, MAX_PROPERTY_OR_LAND_ASSETS, addAssetNowRoute(PropertyOrLand, assets.propertyOrLand)),
      AssetRoute(assets.shares.size, MAX_SHARES_ASSETS, addAssetNowRoute(Shares, assets.shares)),
      AssetRoute(assets.business.size, MAX_BUSINESS_ASSETS, addAssetNowRoute(Business, assets.business)),
      AssetRoute(assets.partnerShip.size, MAX_PARTNERSHIP_ASSETS, addAssetNowRoute(Partnership, assets.partnerShip)),
      AssetRoute(assets.other.size, MAX_OTHER_ASSETS, addAssetNowRoute(Other, assets.other)),
      AssetRoute(assets.nonEEABusiness.size, MAX_NON_EEA_BUSINESS_ASSETS, addAssetNowRoute(NonEeaBusiness, assets.nonEEABusiness))
    )

    routes.filter(x => x.size < x.maxSize) match {
      case x :: Nil => x.route
      case assetsRoutes: Seq[AssetRoute] =>
        AssetNavigator.routeToIndex(
          List.empty,
          controllers.asset.routes.WhatKindOfAssetController.onPageLoad,
          Some(index)
        )
    }
  }

  def addAssetNowRoute(`type`: WhatKindOfAsset,
                       assets: List[AssetType],
                       index: Option[Int] = None): Call = {
    `type` match {
      case Money => routeToMoneyIndex(assets, index)
      case PropertyOrLand => routeToPropertyOrLandIndex(assets, index)
      case Shares => routeToSharesIndex(assets, index)
      case Business => routeToBusinessIndex(assets, index)
      case Partnership => routeToPartnershipIndex(assets, index)
      case Other => routeToOtherIndex(assets, index)
      case NonEeaBusiness => controllers.asset.noneeabusiness.routes.NameController.onPageLoad(index.getOrElse(0), NormalMode)
    }
  }

  private def submitTaskComplete(isMigratingToTaxable: Boolean): Call = {
    if (isMigratingToTaxable) {
      controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.submitComplete()
    } else {
      controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.submitComplete()
    }
  }

  private def maintainATrustOverview: Call = {
    Call(GET, config.maintainATrustOverview)
  }

  private def routeToMoneyIndex(assets: List[AssetType], index: Option[Int]): Call = {
    AssetNavigator.routeToIndexUsingModeCall(
      assets,
      controllers.asset.money.routes.AssetMoneyValueController.onPageLoad,
      index
    )
  }

  private def routeToPropertyOrLandIndex(assets: List[AssetType], index: Option[Int]): Call = {
    AssetNavigator.routeToIndexUsingModeCall(
      assets,
      controllers.asset.property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad,
      index
    )
  }

  private def routeToSharesIndex(assets: List[AssetType], index: Option[Int]): Call = {
    AssetNavigator.routeToIndexUsingModeCall(
      assets,
      controllers.asset.shares.routes.SharesInAPortfolioController.onPageLoad,
      index
    )
  }

  private def routeToBusinessIndex(assets: List[AssetType], index: Option[Int]): Call = {
    AssetNavigator.routeToIndexUsingModeCall(
      assets,
      controllers.asset.business.routes.BusinessNameController.onPageLoad,
      index
    )
  }

  private def routeToPartnershipIndex(assets: List[AssetType], index: Option[Int]): Call = {
    AssetNavigator.routeToIndexUsingModeCall(
      assets,
      controllers.asset.partnership.routes.PartnershipDescriptionController.onPageLoad,
      index
    )
  }

  private def routeToOtherIndex(assets: List[AssetType], index: Option[Int]): Call = {
    AssetNavigator.routeToIndexUsingModeCall(
      assets,
      controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad,
      index
    )
  }
}

object AssetNavigator {

  // taken from 'register-trust-asset-frontend' and modified slighyly
  def routeToIndexUsingModeCall(assets: List[AssetType], route: (Int, Mode) => Call, index: Option[Int] = None): Call = {
    val i = index match {
      case Some(value) => value
      case None => assets.size
    }
    route(i, NormalMode)
  }

  def routeToIndex(assets: List[AssetType], route: Int => Call, index: Option[Int] = None): Call = {
    val i = index match {
      case Some(value) => value
      case None => assets.size
    }
    route(i)
  }

}
