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
import pages.asset.{AddAnAssetYesNoPage, AddAssetsPage, WhatKindOfAssetPage}
import play.api.mvc.Call

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[Navigator]
  val index = 0

  private val assetsCompletedRoute: Call = {
    Call("GET", frontendAppConfig.registrationProgressUrl(fakeDraftId))
  }

  "Navigator" must {

    "go to WhatKindOfAssetPage from from AddAnAssetYesNoPage when selected Yes" in {
      val index = 0

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(AddAnAssetYesNoPage, true).success.value

          navigator.nextPage(AddAnAssetYesNoPage, fakeDraftId)(answers)
            .mustBe(controllers.asset.routes.WhatKindOfAssetController.onPageLoad(index, fakeDraftId))
      }
    }

    "go to RegistrationProgress from from AddAnAssetYesNoPage when selected No" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(AddAnAssetYesNoPage, false).success.value

          navigator.nextPage(AddAnAssetYesNoPage, fakeDraftId)(answers)
            .mustBe(assetsCompletedRoute)
      }
    }

    "add another asset" must {

      "go to the WhatKindOfAssetPage from AddAssetsPage when selected add them now" in {

        val answers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), Money).success.value
          .set(AddAssetsPage, AddAssets.YesNow).success.value

        navigator.nextPage(AddAssetsPage, fakeDraftId)(answers)
          .mustBe(controllers.asset.routes.WhatKindOfAssetController.onPageLoad(1, fakeDraftId))
      }

      "go to RegistrationProgress from AddAssetsPage when selecting add them later" in {

        val answers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), Money).success.value
          .set(AddAssetsPage, AddAssets.YesLater).success.value

        navigator.nextPage(AddAssetsPage, fakeDraftId)(answers)
          .mustBe(assetsCompletedRoute)
      }

      "go to RegistrationProgress from AddAssetsPage when selecting no complete" in {

        val answers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), Money).success.value
          .set(AddAssetsPage, AddAssets.NoComplete).success.value

        navigator.nextPage(AddAssetsPage, fakeDraftId)(answers)
          .mustBe(assetsCompletedRoute)
      }
    }

    "what kind of asset page" when {

      "go to AssetMoneyValuePage when money is selected" in {
        val index = 0

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), Money).success.value

            navigator.nextPage(WhatKindOfAssetPage(index), fakeDraftId)(answers)
              .mustBe(money.routes.AssetMoneyValueController.onPageLoad(index, fakeDraftId))
        }
      }

      "go to PropertyOrLandAddressYesNoController when PropertyOrLand is selected" in {
        val index = 0

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), PropertyOrLand).success.value

            navigator.nextPage(WhatKindOfAssetPage(index), fakeDraftId)(answers)
              .mustBe(property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(index, fakeDraftId))
        }
      }

      "go to SharesInAPortfolio from WhatKindOfAsset when Shares is selected" in {
        val index = 0

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), Shares).success.value

            navigator.nextPage(WhatKindOfAssetPage(index), fakeDraftId)(answers)
              .mustBe(shares.routes.SharesInAPortfolioController.onPageLoad(index, fakeDraftId))
        }
      }

      "go to business asset name from WhatKindOfAsset when Business is selected" in {
        val index = 0

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), Business).success.value

            navigator.nextPage(WhatKindOfAssetPage(index), fakeDraftId)(answers)
              .mustBe(business.routes.BusinessNameController.onPageLoad(index, fakeDraftId))
        }
      }

      "go to partnership asset description from WhatKindOfAsset when Partnership is selected" in {
        val index = 0

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), Partnership).success.value

            navigator.nextPage(WhatKindOfAssetPage(index), fakeDraftId)(answers)
              .mustBe(partnership.routes.PartnershipDescriptionController.onPageLoad(index, fakeDraftId))
        }
      }

      "go to other asset description when Other is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), Other).success.value

            navigator.nextPage(WhatKindOfAssetPage(index), fakeDraftId)(answers)
              .mustBe(other.routes.OtherAssetDescriptionController.onPageLoad(index, fakeDraftId))
        }
      }

      "go to non-EEA business asset name when NonEeaBusiness is selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>

            val answers = userAnswers.set(WhatKindOfAssetPage(index), NonEeaBusiness).success.value

            navigator.nextPage(WhatKindOfAssetPage(index), fakeDraftId)(answers)
              .mustBe(noneeabusiness.routes.NameController.onPageLoad(index, fakeDraftId))
        }
      }
    }
  }
}
