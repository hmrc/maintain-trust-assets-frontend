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
import models.{AddAssets, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.{AddAnAssetYesNoPage, AddAssetsPage, AssetInterruptPage, TrustOwnsNonEeaBusinessYesNoPage, WhatKindOfAssetPage}
import play.api.mvc.Call

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[Navigator]
  private val index = 0

  private val assetsCompletedRoute: Call = {
    Call("GET", frontendAppConfig.registrationProgressUrl())
  }

  "Navigator" when {

    "trust owns non-EEA business yes no page" when {

      "yes selected" must {
        "redirect to interrupt page" in {

          val answers = emptyUserAnswers.set(TrustOwnsNonEeaBusinessYesNoPage, true).success.value

          navigator.nextPage(TrustOwnsNonEeaBusinessYesNoPage)(answers)
            .mustBe(controllers.asset.routes.AssetInterruptPageController.onPageLoad())
        }
      }

      "no selected" must {
        "redirect to RegistrationProgress" in {

          val answers = emptyUserAnswers.set(TrustOwnsNonEeaBusinessYesNoPage, false).success.value

          navigator.nextPage(TrustOwnsNonEeaBusinessYesNoPage)(answers)
            .mustBe(assetsCompletedRoute)
        }
      }
    }

    "asset interrupt page" when {

      "taxable" must {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = true)

        "redirect to WhatKindOfAssetPage" in {

          navigator.nextPage(AssetInterruptPage)(baseAnswers)
            .mustBe(controllers.asset.routes.WhatKindOfAssetController.onPageLoad(index))
        }
      }

      "non-taxable" must {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = false)

        "redirect to non-EEA business asset name page" in {

          val answers = baseAnswers.set(WhatKindOfAssetPage(0), NonEeaBusiness).success.value

          navigator.nextPage(AssetInterruptPage)(answers)
            .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(0))
        }
      }
    }

    "add an asset yes no page" when {

      "taxable" when {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = true)

        "yes selected" must {
          "redirect to WhatKindOfAssetPage" in {

            val answers = baseAnswers.set(AddAnAssetYesNoPage, true).success.value

            navigator.nextPage(AddAnAssetYesNoPage)(answers)
              .mustBe(controllers.asset.routes.WhatKindOfAssetController.onPageLoad(index))
          }
        }

        "no selected" must {
          "redirect to RegistrationProgress" in {

            val answers = baseAnswers.set(AddAnAssetYesNoPage, false).success.value

            navigator.nextPage(AddAnAssetYesNoPage)(answers)
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
              .set(WhatKindOfAssetPage(0), NonEeaBusiness).success.value

            navigator.nextPage(AddAnAssetYesNoPage)(answers)
              .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(0))
          }
        }

        "no selected" must {
          "redirect to RegistrationProgress" in {

            val answers = baseAnswers.set(AddAnAssetYesNoPage, false).success.value

            navigator.nextPage(AddAnAssetYesNoPage)(answers)
              .mustBe(assetsCompletedRoute)
          }
        }
      }
    }

    "add assets page" when {

      "taxable" when {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = true)

        "add them now selected" must {
          "go to the WhatKindOfAssetPage" in {

            val answers = baseAnswers
              .set(WhatKindOfAssetPage(0), Money).success.value
              .set(AddAssetsPage, AddAssets.YesNow).success.value

            navigator.nextPage(AddAssetsPage)(answers)
              .mustBe(controllers.asset.routes.WhatKindOfAssetController.onPageLoad(1))
          }
        }

        "add them later selected" must {
          "go to RegistrationProgress" in {

            val answers = baseAnswers
              .set(WhatKindOfAssetPage(0), Money).success.value
              .set(AddAssetsPage, AddAssets.YesLater).success.value

            navigator.nextPage(AddAssetsPage)(answers)
              .mustBe(assetsCompletedRoute)
          }
        }

        "no complete selected" must {
          "go to RegistrationProgress" in {

            val answers = baseAnswers
              .set(WhatKindOfAssetPage(0), Money).success.value
              .set(AddAssetsPage, AddAssets.NoComplete).success.value

            navigator.nextPage(AddAssetsPage)(answers)
              .mustBe(assetsCompletedRoute)
          }
        }
      }

      "non-taxable" when {

        val baseAnswers = emptyUserAnswers.copy(isTaxable = false)

        "add them now selected" must {
          "go to the non-EEA business asset name page" in {

            val answers = baseAnswers
              .set(WhatKindOfAssetPage(0), Money).success.value
              .set(WhatKindOfAssetPage(1), NonEeaBusiness).success.value
              .set(AddAssetsPage, AddAssets.YesNow).success.value

            navigator.nextPage(AddAssetsPage)(answers)
              .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(1))
          }
        }

        "add them later selected" must {
          "go to RegistrationProgress" in {

            val answers = baseAnswers
              .set(WhatKindOfAssetPage(0), Money).success.value
              .set(AddAssetsPage, AddAssets.YesLater).success.value

            navigator.nextPage(AddAssetsPage)(answers)
              .mustBe(assetsCompletedRoute)
          }
        }

        "no complete selected" must {
          "go to RegistrationProgress" in {

            val answers = baseAnswers
              .set(WhatKindOfAssetPage(0), Money).success.value
              .set(AddAssetsPage, AddAssets.NoComplete).success.value

            navigator.nextPage(AddAssetsPage)(answers)
              .mustBe(assetsCompletedRoute)
          }
        }
      }
    }

    "what kind of asset page" when {

      "go to AssetMoneyValuePage when money is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), Money).success.value

            navigator.nextPage(WhatKindOfAssetPage(index))(answers)
              .mustBe(money.routes.AssetMoneyValueController.onPageLoad(index))
        }
      }

      "go to PropertyOrLandAddressYesNoController when PropertyOrLand is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), PropertyOrLand).success.value

            navigator.nextPage(WhatKindOfAssetPage(index))(answers)
              .mustBe(property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(index))
        }
      }

      "go to SharesInAPortfolio from WhatKindOfAsset when Shares is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), Shares).success.value

            navigator.nextPage(WhatKindOfAssetPage(index))(answers)
              .mustBe(shares.routes.SharesInAPortfolioController.onPageLoad(index))
        }
      }

      "go to business asset name from WhatKindOfAsset when Business is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), Business).success.value

            navigator.nextPage(WhatKindOfAssetPage(index))(answers)
              .mustBe(business.routes.BusinessNameController.onPageLoad(index))
        }
      }

      "go to partnership asset description from WhatKindOfAsset when Partnership is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), Partnership).success.value

            navigator.nextPage(WhatKindOfAssetPage(index))(answers)
              .mustBe(partnership.routes.PartnershipDescriptionController.onPageLoad(index))
        }
      }

      "go to other asset description when Other is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), Other).success.value

            navigator.nextPage(WhatKindOfAssetPage(index))(answers)
              .mustBe(other.routes.OtherAssetDescriptionController.onPageLoad(index))
        }
      }

      "go to non-EEA business asset name when NonEeaBusiness is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), NonEeaBusiness).success.value

            navigator.nextPage(WhatKindOfAssetPage(index))(answers)
              .mustBe(noneeabusiness.routes.NameController.onPageLoad(index))
        }
      }
    }
  }
}
