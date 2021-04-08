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
import controllers.asset._
import generators.Generators
import models.WhatKindOfAsset._
import models.{AddAssets, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.{AddAnAssetYesNoPage, AddAssetsPage, AssetInterruptPage, TrustOwnsNonEeaBusinessYesNoPage, WhatKindOfAssetPage}
import play.api.mvc.Call

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[AssetsNavigator]

  private val assetsCompletedRoute: Call = {
    controllers.asset.routes.AddAssetsController.submitComplete()
  }

  "Navigator" when {

    "trust owns non-EEA business yes no page" when {

      "yes selected" must {
        "redirect to interrupt page" in {

          val answers = emptyUserAnswers.set(TrustOwnsNonEeaBusinessYesNoPage, true).success.value

          navigator.nextPage(TrustOwnsNonEeaBusinessYesNoPage, NormalMode, answers)
            .mustBe(controllers.asset.routes.AssetInterruptPageController.onPageLoad())
        }
      }

      "no selected" must {
        "redirect to MaintenanceProgress" in {

          val answers = emptyUserAnswers.set(TrustOwnsNonEeaBusinessYesNoPage, false).success.value

          navigator.nextPage(TrustOwnsNonEeaBusinessYesNoPage, NormalMode, answers)
            .mustBe(assetsCompletedRoute)
        }
      }
    }

    "asset interrupt page" when {

      "taxable" must {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = true)

        "redirect to non-EEA business asset name page" in {

          navigator.nextPage(AssetInterruptPage, NormalMode, baseAnswers)
            .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
        }
      }

      "non-taxable" must {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = false)

        "redirect to non-EEA business asset name page" in {

          val answers = baseAnswers.set(WhatKindOfAssetPage, NonEeaBusiness).success.value

          navigator.nextPage(AssetInterruptPage, NormalMode, answers)
            .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
        }
      }
    }

    "add an asset yes no page" when {

      "taxable" when {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = true)

        "yes selected" must {
          "redirect to non-EEA business asset name page" in {

            val answers = baseAnswers.set(AddAnAssetYesNoPage, true).success.value

            navigator.nextPage(AddAnAssetYesNoPage, NormalMode, answers)
              .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
          }
        }

        "no selected" must {
          "redirect to MaintenanceProgress" in {

            val answers = baseAnswers.set(AddAnAssetYesNoPage, false).success.value

            navigator.nextPage(AddAnAssetYesNoPage, NormalMode, answers)
              .mustBe(assetsCompletedRoute)
          }
        }
      }

      "non-taxable" when {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = false)

        "yes selected" must {
          "redirect to non-EEA business asset name page" in {

            val answers = baseAnswers
              .set(AddAnAssetYesNoPage, true).success.value
              .set(WhatKindOfAssetPage, NonEeaBusiness).success.value

            navigator.nextPage(AddAnAssetYesNoPage, NormalMode, answers)
              .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
          }
        }

        "no selected" must {
          "redirect to MaintenanceProgress" in {

            val answers = baseAnswers.set(AddAnAssetYesNoPage, false).success.value

            navigator.nextPage(AddAnAssetYesNoPage, NormalMode, answers)
              .mustBe(assetsCompletedRoute)
          }
        }
      }
    }

    "add assets page" when {

      "taxable" when {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = true)

        "add them now selected" must {
          "go to the non-EEA business asset name page" in {
            val answers = baseAnswers
              .set(WhatKindOfAssetPage, Money).success.value
              .set(AddAssetsPage, AddAssets.YesNow).success.value

            navigator.nextPage(AddAssetsPage, NormalMode, answers)
              .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
          }
        }

        "no complete selected" must {
          "go to MaintenanceProgress" in {

            val answers = baseAnswers
              .set(WhatKindOfAssetPage, Money).success.value
              .set(AddAssetsPage, AddAssets.NoComplete).success.value

            navigator.nextPage(AddAssetsPage, NormalMode, answers)
              .mustBe(assetsCompletedRoute)
          }
        }
      }

      "non-taxable" when {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = false)

        "add them now selected" must {
          "go to the non-EEA business asset name page" in {

            val answers = baseAnswers
              .set(WhatKindOfAssetPage, Money).success.value
              .set(WhatKindOfAssetPage, NonEeaBusiness).success.value
              .set(AddAssetsPage, AddAssets.YesNow).success.value

            navigator.nextPage(AddAssetsPage, NormalMode, answers)
              .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
          }
        }

        "no complete selected" must {
          "go to RegistrationProgress" in {

            val answers = baseAnswers
              .set(WhatKindOfAssetPage, Money).success.value
              .set(AddAssetsPage, AddAssets.NoComplete).success.value

            navigator.nextPage(AddAssetsPage, NormalMode, answers)
              .mustBe(assetsCompletedRoute)
          }
        }
      }
    }

    "what kind of asset page" when {

      "go to AssetMoneyValuePage when money is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage, Money).success.value

            navigator.nextPage(WhatKindOfAssetPage, NormalMode, answers)
              .mustBe(money.routes.AssetMoneyValueController.onPageLoad(NormalMode))
        }
      }

      "go to PropertyOrLandAddressYesNoController when PropertyOrLand is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage, PropertyOrLand).success.value

            navigator.nextPage(WhatKindOfAssetPage, NormalMode, answers)
              .mustBe(property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode))
        }
      }

      "go to SharesInAPortfolio from WhatKindOfAsset when Shares is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage, Shares).success.value

            navigator.nextPage(WhatKindOfAssetPage, NormalMode, answers)
              .mustBe(shares.routes.SharesInAPortfolioController.onPageLoad(NormalMode))
        }
      }

      "go to business asset name from WhatKindOfAsset when Business is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage, Business).success.value

            navigator.nextPage(WhatKindOfAssetPage, NormalMode, answers)
              .mustBe(business.routes.BusinessNameController.onPageLoad(NormalMode))
        }
      }

      "go to partnership asset description from WhatKindOfAsset when Partnership is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage, Partnership).success.value

            navigator.nextPage(WhatKindOfAssetPage, NormalMode, answers)
              .mustBe(partnership.routes.PartnershipDescriptionController.onPageLoad(NormalMode))
        }
      }

      "go to other asset description when Other is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage, Other).success.value

            navigator.nextPage(WhatKindOfAssetPage, NormalMode, answers)
              .mustBe(other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode))
        }
      }

      "go to non-EEA business asset name when NonEeaBusiness is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage, NonEeaBusiness).success.value

            navigator.nextPage(WhatKindOfAssetPage, NormalMode, answers)
              .mustBe(noneeabusiness.routes.NameController.onPageLoad(NormalMode))
        }
      }
    }
  }
}
