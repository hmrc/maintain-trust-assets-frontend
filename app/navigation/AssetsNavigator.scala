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
import controllers.Assets.Redirect
import controllers.asset.routes.AssetInterruptPageController
import models.Constants._
import models.WhatKindOfAsset.{Business, Money, NonEeaBusiness, Other, Partnership, PropertyOrLand, Shares}
import models.assets.Assets
import models.{NormalMode, WhatKindOfAsset}
import play.api.mvc.{Call, Result}
import uk.gov.hmrc.http.HttpVerbs.GET

import javax.inject.Inject

class AssetsNavigator @Inject()(config: FrontendAppConfig) {

  def redirectToAddAssetPage(isMigratingToTaxable: Boolean): Result = Redirect {
    if (isMigratingToTaxable) {
      controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
    } else {
      controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad()
    }
  }

  def redirectFromInterruptPage(isMigratingToTaxable: Boolean, noAssets: Boolean): Result = Redirect {
    (isMigratingToTaxable, noAssets)  match {
      case (true, true) => controllers.asset.routes.WhatKindOfAssetController.onPageLoad()
      case (true, false)  => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
      case (false, _) => controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode)
    }
  }

  def redirectFromAddAssetYesNoPage(value: Boolean, isMigratingToTaxable: Boolean, noAssets: Boolean): Call = {
    if (isMigratingToTaxable) {
      if (value) {
        AssetInterruptPageController.onPageLoad()
      } else {
        if (noAssets) {
          maintainATrustOverview
        } else {
          controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
        }
      }
    } else {
      if (value) {
        controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode)
      } else {
        submitTaskComplete(isMigratingToTaxable)
      }
    }
  }

  def redirectFromEntryQuestion(value: Boolean, isMigratingToTaxable: Boolean): Call = {
    if (value) {
      AssetInterruptPageController.onPageLoad()
    } else {
      submitTaskComplete(isMigratingToTaxable)
    }
  }

  def addAssetRoute(assets: Assets): Call = {

    case class AssetRoute(size: Int, maxSize: Int, route: Call)

    val routes: List[AssetRoute] = List(
      AssetRoute(assets.monetary.size, MAX_MONEY_ASSETS, addAssetNowRoute(Money)),
      AssetRoute(assets.propertyOrLand.size, MAX_PROPERTY_OR_LAND_ASSETS, addAssetNowRoute(PropertyOrLand)),
      AssetRoute(assets.shares.size, MAX_SHARES_ASSETS, addAssetNowRoute(Shares)),
      AssetRoute(assets.business.size, MAX_BUSINESS_ASSETS, addAssetNowRoute(Business)),
      AssetRoute(assets.partnerShip.size, MAX_PARTNERSHIP_ASSETS, addAssetNowRoute(Partnership)),
      AssetRoute(assets.other.size, MAX_OTHER_ASSETS, addAssetNowRoute(Other)),
      AssetRoute(assets.nonEEABusiness.size, MAX_NON_EEA_BUSINESS_ASSETS, addAssetNowRoute(NonEeaBusiness))
    )

    routes.filter(x => x.size < x.maxSize) match {
      case x :: Nil => x.route
      case _ => controllers.asset.routes.WhatKindOfAssetController.onPageLoad()
    }
  }

  def addAssetNowRoute(`type`: WhatKindOfAsset): Call = {
    `type` match {
      case Money => controllers.asset.money.routes.AssetMoneyValueController.onPageLoad(NormalMode)
      case PropertyOrLand => controllers.asset.property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode)
      case Shares => controllers.asset.shares.routes.SharesInAPortfolioController.onPageLoad(NormalMode)
      case Business => controllers.asset.business.routes.BusinessNameController.onPageLoad(NormalMode)
      case Partnership => controllers.asset.partnership.routes.PartnershipDescriptionController.onPageLoad(NormalMode)
      case Other => controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode)
      case NonEeaBusiness => controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode)
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

}
