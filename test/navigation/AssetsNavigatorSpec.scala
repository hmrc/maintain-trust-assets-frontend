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

import base.SpecBase
import controllers.asset.nonTaxableToTaxable.{routes => nonTaxToTaxRts}
import controllers.asset.{routes => rts}
import generators.Generators
import models.AddAssets.{NoComplete, YesNow}
import models.NormalMode
import models.WhatKindOfAsset.{Business, Money, NonEeaBusiness, Other, Partnership, PropertyOrLand, Shares}
import models.assets.{AssetMonetaryAmount, Assets}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.nontaxabletotaxable.AddAssetsYesNoPage
import pages.asset._
import play.api.mvc.Call

class AssetsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[AssetsNavigator]

  private val existingAssets = Assets(List(AssetMonetaryAmount(4000L)))

  "Assets Navigator" must {

    "navigate from AssetInterruptPage to WhatKindOfAssetController when no assets exist" in {

      val page = AssetInterruptPage

      val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)

      navigator.nextPage(page, userAnswers, Assets())
        .mustBe(rts.WhatKindOfAssetController.onPageLoad())
    }

    "navigate from AssetInterruptPage to AddAssetsController when assets exist" in {

      val page = AssetInterruptPage

      val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)

      navigator.nextPage(page, userAnswers, existingAssets)
        .mustBe(nonTaxToTaxRts.AddAssetsController.onPageLoad())
    }

    "navigate from AssetInterruptPage to NameController when not migrating to taxable" in {

      val page = AssetInterruptPage

      val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = false)

      navigator.nextPage(page, userAnswers)
        .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
    }

    "navigate from WhatKindOfAssetPage" when {

      val page = WhatKindOfAssetPage

      "Money is selected to AssetMoneyValueController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, Money).success.value

        navigator.nextPage(page, userAnswers, Assets())
          .mustBe(controllers.asset.money.routes.AssetMoneyValueController.onPageLoad(NormalMode))
      }

      "PropertyOrLand is selected to PropertyOrLandAddressYesNoController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, PropertyOrLand).success.value

        navigator.nextPage(page, userAnswers, Assets())
          .mustBe(controllers.asset.property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode))
      }

      "Shares is selected to SharesInAPortfolioController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, Shares).success.value

        navigator.nextPage(page, userAnswers, Assets())
          .mustBe(controllers.asset.shares.routes.SharesInAPortfolioController.onPageLoad(NormalMode))
      }

      "Business is selected to BusinessNameController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, Business).success.value

        navigator.nextPage(page, userAnswers, Assets())
          .mustBe(controllers.asset.business.routes.BusinessNameController.onPageLoad(NormalMode))
      }

      "Partnership is selected to PartnershipDescriptionController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, Partnership).success.value

        navigator.nextPage(page, userAnswers, Assets())
          .mustBe(controllers.asset.partnership.routes.PartnershipDescriptionController.onPageLoad(NormalMode))
      }

      "Other is selected to OtherAssetDescriptionController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, Other).success.value

        navigator.nextPage(page, userAnswers, Assets())
          .mustBe(controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode))
      }

      "NonEeaBusiness is selected to NameController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, NonEeaBusiness).success.value

        navigator.nextPage(page, userAnswers, Assets())
          .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
      }

    }

    "navigate from AddAssetsPage" when {

      val page = AddAssetsPage

      "yes is selected and assets exist, to AddAssetsController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, YesNow).success.value

        navigator.nextPage(page, userAnswers, existingAssets)
          .mustBe(nonTaxToTaxRts.AddAssetsController.onPageLoad())
      }

      "yes is selected and no assets exist, to WhatKindOfAssetController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, YesNow).success.value

        navigator.nextPage(page, userAnswers)
          .mustBe(rts.WhatKindOfAssetController.onPageLoad())
      }

      "yes is selected and not migrating to taxable, to NameController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = false)
          .set(page, YesNow).success.value

        navigator.nextPage(page, userAnswers)
          .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
      }

      "no is selected, to AddNonEeaBusinessAssetController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, NoComplete).success.value

        navigator.nextPage(page, userAnswers)
          .mustBe(controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.submitComplete())
      }
    }

    "navigate from AddAssetsYesNoPage" when {

      val page = AddAssetsYesNoPage

      "yes selected, to AssetInterruptPageController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, true).success.value

        navigator.nextPage(page, userAnswers)
          .mustBe(rts.AssetInterruptPageController.onPageLoad())
      }

      "no selected and assets exist, to AddAssetsController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, false).success.value

        navigator.nextPage(page, userAnswers, existingAssets)
          .mustBe(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
      }

      "no selected and no assets exist, to Maintain A Trust Overview" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, false).success.value

        navigator.nextPage(page, userAnswers)
          .mustBe(Call("GET", "http://localhost:9788/maintain-a-trust/overview"))
      }
    }

    "navigate from AddAnAssetYesNoPage" when {

      val page = AddAnAssetYesNoPage

      "yes is selected and assets exist, to AddAssetsController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, true).success.value

        navigator.nextPage(page, userAnswers, existingAssets)
          .mustBe(nonTaxToTaxRts.AddAssetsController.onPageLoad())
      }

      "yes is selected and no assets exist, to WhatKindOfAssetController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, true).success.value

        navigator.nextPage(page, userAnswers)
          .mustBe(rts.WhatKindOfAssetController.onPageLoad())
      }

      "yes is selected and not migrating to taxable, to NameController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = false)
          .set(page, true).success.value

        navigator.nextPage(page, userAnswers)
          .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
      }

      "no is selected, to AddNonEeaBusinessAssetController" in {
        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, false).success.value

        navigator.nextPage(page, userAnswers)
          .mustBe(controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.submitComplete())
      }
    }

    "navigate from TrustOwnsNonEeaBusinessYesNoPage" when {

      val page = TrustOwnsNonEeaBusinessYesNoPage

      "yes selected, to AssetInterruptPageController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, true).success.value

        navigator.nextPage(page, userAnswers)
          .mustBe(rts.AssetInterruptPageController.onPageLoad())
      }

      "no selected, to AddNonEeaBusinessAssetController" in {

        val userAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
          .set(page, false).success.value

        navigator.nextPage(page, userAnswers, existingAssets)
          .mustBe(controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.submitComplete())
      }
    }
  }
}
