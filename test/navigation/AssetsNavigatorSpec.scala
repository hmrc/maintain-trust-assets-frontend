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

import base.SpecBase
import controllers.asset.nonTaxableToTaxable.{routes => nonTaxToTaxRts}
import controllers.asset.{routes => rts}
import generators.Generators
import models.Constants._
import models.WhatKindOfAsset._
import models.assets._
import models.{NormalMode, ShareClass, UkAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.Call
import uk.gov.hmrc.http.HttpVerbs.GET
import utils.Constants.QUOTED

import java.time.LocalDate

class AssetsNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: AssetsNavigator = injector.instanceOf[AssetsNavigator]

  "Assets Navigator" when {

    "redirectToAddAssetPage" when {

      "migrating from non-taxable to taxable" must {
        "redirect to taxable add-to page" in {

          navigator.redirectToAddAssetPage(isMigratingToTaxable = true)
            .mustBe(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
        }
      }

      "not migrating from non-taxable to taxable" must {
        "redirect to non-taxable add-to page" in {

          navigator.redirectToAddAssetPage(isMigratingToTaxable = false)
            .mustBe(controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad())
        }
      }
    }

    "redirectFromInterruptPage" when {

      "migrating from non-taxable to taxable" when {

        "no assets exist" must {
          "redirect to what-kind-of-asset page" in {

            navigator.redirectFromInterruptPage(isMigratingToTaxable = true, noAssets = true)
              .mustBe(rts.WhatKindOfAssetController.onPageLoad())
          }
        }

        "assets exist" must {
          "redirect to add-to page" in {

            navigator.redirectFromInterruptPage(isMigratingToTaxable = true, noAssets = false)
              .mustBe(nonTaxToTaxRts.AddAssetsController.onPageLoad())
          }
        }
      }

      "not migrating from non-taxable to taxable" must {
        "redirect to non-EEA business name page" in {
          forAll(arbitrary[Boolean]) { bool =>
            navigator.redirectFromInterruptPage(isMigratingToTaxable = false, bool)
              .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
          }
        }
      }
    }

    "redirectFromAddAssetYesNoPage" when {

      "migrating from non-taxable to taxable" when {

        "yes selected" must {
          "redirect to interrupt page" in {
            forAll(arbitrary[Boolean]) { bool =>
              navigator.redirectFromAddAssetYesNoPage(value = true, isMigratingToTaxable = true, noAssets = bool)
                .mustBe(controllers.asset.routes.AssetInterruptPageController.onPageLoad())
            }
          }
        }

        "no selected" when {

          "no assets" must {
            "redirect to task list" in {
              navigator.redirectFromAddAssetYesNoPage(value = false, isMigratingToTaxable = true, noAssets = true)
                .mustBe(Call(GET, frontendAppConfig.maintainATrustOverview))
            }
          }

          "there are assets" must {
            "redirect to taxable add-to page" in {
              navigator.redirectFromAddAssetYesNoPage(value = false, isMigratingToTaxable = true, noAssets = false)
                .mustBe(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
            }
          }
        }
      }

      "not migrating from non-taxable to taxable" when {

        "yes selected" must {
          "redirect to non-EEA business name page" in {
            forAll(arbitrary[Boolean]) { bool =>
              navigator.redirectFromAddAssetYesNoPage(value = true, isMigratingToTaxable = false, noAssets = bool)
                .mustBe(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
            }
          }
        }

        "no selected" must {
          "redirect to submit-complete route" in {
            forAll(arbitrary[Boolean]) { bool =>
              navigator.redirectFromAddAssetYesNoPage(value = false, isMigratingToTaxable = false, noAssets = bool)
                .mustBe(controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.submitComplete())
            }
          }
        }
      }
    }

    "redirectFromEntryQuestion" when {
      "yes selected" must {
        "redirect to interrupt page" in {
          forAll(arbitrary[Boolean]) { bool =>
            navigator.redirectFromEntryQuestion(value = true, isMigratingToTaxable = bool)
              .mustBe(controllers.asset.routes.AssetInterruptPageController.onPageLoad())
          }
        }
      }

      "no selected" when {

        "migrating from non-taxable to taxable" must {
          "redirect to submit-complete route" in {
            navigator.redirectFromEntryQuestion(value = false, isMigratingToTaxable = true)
              .mustBe(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.submitComplete())
          }
        }

        "not migrating from non-taxable to taxable" must {
          "redirect to submit-complete route" in {
            navigator.redirectFromEntryQuestion(value = false, isMigratingToTaxable = false)
              .mustBe(controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.submitComplete())
          }
        }
      }
    }

    "addAssetRoute" when {

      val value = 4000L

      val money = AssetMonetaryAmount(assetMonetaryAmount = value)

      val propertyOrLand = PropertyLandType(
        buildingLandName = None,
        address = None,
        valueFull = value,
        valuePrevious = None
      )

      val share = SharesType(
        numberOfShares = "1",
        orgName = "Share",
        shareClass = ShareClass.Ordinary.toString,
        typeOfShare = QUOTED,
        value = value,
        isPortfolio = None
      )

      val business = BusinessAssetType(
        orgName = "Business",
        businessDescription = "Description",
        address = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB"),
        businessValue = value
      )

      val partnership = PartnershipType(
        description = "Description",
        partnershipStart = LocalDate.parse("2020-01-01")
      )

      val other = OtherAssetType(
        description = "Description",
        value = value
      )

      val nonEeaBusiness = NonEeaBusinessType(
        lineNo = None,
        orgName = "Business",
        address = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB"),
        govLawCountry = "GB",
        startDate = LocalDate.parse("2020-01-01"),
        endDate = None,
        provisional = false
      )

      "all types maxed out except individual" must {
        "redirect to money journey" in {

          val assets = Assets(
            monetary = Nil,
            propertyOrLand = List.fill(MAX_PROPERTY_OR_LAND_ASSETS)(propertyOrLand),
            shares = List.fill(MAX_SHARES_ASSETS)(share),
            business = List.fill(MAX_BUSINESS_ASSETS)(business),
            partnerShip = List.fill(MAX_PARTNERSHIP_ASSETS)(partnership),
            other = List.fill(MAX_OTHER_ASSETS)(other),
            nonEEABusiness = List.fill(MAX_NON_EEA_BUSINESS_ASSETS)(nonEeaBusiness)
          )

          navigator.addAssetRoute(assets).url mustBe
            controllers.asset.money.routes.AssetMoneyValueController.onPageLoad(NormalMode).url
        }
      }

      "all types maxed out except property or land" must {
        "redirect to property or land journey" in {

          val assets = Assets(
            monetary = List.fill(MAX_MONEY_ASSETS)(money),
            propertyOrLand = Nil,
            shares = List.fill(MAX_SHARES_ASSETS)(share),
            business = List.fill(MAX_BUSINESS_ASSETS)(business),
            partnerShip = List.fill(MAX_PARTNERSHIP_ASSETS)(partnership),
            other = List.fill(MAX_OTHER_ASSETS)(other),
            nonEEABusiness = List.fill(MAX_NON_EEA_BUSINESS_ASSETS)(nonEeaBusiness)
          )

          navigator.addAssetRoute(assets).url mustBe
            controllers.asset.property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode).url
        }
      }

      "all types maxed out except shares" must {
        "redirect to shares journey" in {

          val assets = Assets(
            monetary = List.fill(MAX_MONEY_ASSETS)(money),
            propertyOrLand = List.fill(MAX_PROPERTY_OR_LAND_ASSETS)(propertyOrLand),
            shares = Nil,
            business = List.fill(MAX_BUSINESS_ASSETS)(business),
            partnerShip = List.fill(MAX_PARTNERSHIP_ASSETS)(partnership),
            other = List.fill(MAX_OTHER_ASSETS)(other),
            nonEEABusiness = List.fill(MAX_NON_EEA_BUSINESS_ASSETS)(nonEeaBusiness)
          )

          navigator.addAssetRoute(assets).url mustBe
            controllers.asset.shares.routes.SharesInAPortfolioController.onPageLoad(NormalMode).url
        }
      }

      "all types maxed out except business" must {
        "redirect to business journey" in {

          val assets = Assets(
            monetary = List.fill(MAX_MONEY_ASSETS)(money),
            propertyOrLand = List.fill(MAX_PROPERTY_OR_LAND_ASSETS)(propertyOrLand),
            shares = List.fill(MAX_SHARES_ASSETS)(share),
            business = Nil,
            partnerShip = List.fill(MAX_PARTNERSHIP_ASSETS)(partnership),
            other = List.fill(MAX_OTHER_ASSETS)(other),
            nonEEABusiness = List.fill(MAX_NON_EEA_BUSINESS_ASSETS)(nonEeaBusiness)
          )

          navigator.addAssetRoute(assets).url mustBe
            controllers.asset.business.routes.BusinessNameController.onPageLoad(NormalMode).url
        }
      }

      "all types maxed out except partnership" must {
        "redirect to partnership journey" in {

          val assets = Assets(
            monetary = List.fill(MAX_MONEY_ASSETS)(money),
            propertyOrLand = List.fill(MAX_PROPERTY_OR_LAND_ASSETS)(propertyOrLand),
            shares = List.fill(MAX_SHARES_ASSETS)(share),
            business = List.fill(MAX_BUSINESS_ASSETS)(business),
            partnerShip = Nil,
            other = List.fill(MAX_OTHER_ASSETS)(other),
            nonEEABusiness = List.fill(MAX_NON_EEA_BUSINESS_ASSETS)(nonEeaBusiness)
          )

          navigator.addAssetRoute(assets).url mustBe
            controllers.asset.partnership.routes.PartnershipDescriptionController.onPageLoad(NormalMode).url
        }
      }

      "all types maxed out except other" must {
        "redirect to other journey" in {

          val assets = Assets(
            monetary = List.fill(MAX_MONEY_ASSETS)(money),
            propertyOrLand = List.fill(MAX_PROPERTY_OR_LAND_ASSETS)(propertyOrLand),
            shares = List.fill(MAX_SHARES_ASSETS)(share),
            business = List.fill(MAX_BUSINESS_ASSETS)(business),
            partnerShip = List.fill(MAX_PARTNERSHIP_ASSETS)(partnership),
            other = Nil,
            nonEEABusiness = List.fill(MAX_NON_EEA_BUSINESS_ASSETS)(nonEeaBusiness)
          )

          navigator.addAssetRoute(assets).url mustBe
            controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode).url
        }
      }

      "all types maxed out except non-EEA business" must {
        "redirect to non-EEA business journey" in {

          val assets = Assets(
            monetary = List.fill(MAX_MONEY_ASSETS)(money),
            propertyOrLand = List.fill(MAX_PROPERTY_OR_LAND_ASSETS)(propertyOrLand),
            shares = List.fill(MAX_SHARES_ASSETS)(share),
            business = List.fill(MAX_BUSINESS_ASSETS)(business),
            partnerShip = List.fill(MAX_PARTNERSHIP_ASSETS)(partnership),
            other = List.fill(MAX_OTHER_ASSETS)(other),
            nonEEABusiness = Nil
          )

          navigator.addAssetRoute(assets).url mustBe
            controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode).url
        }
      }

      "more than one type that isn't maxed out" must {
        "redirect to what-kind-of-asset page" when {

          "no types maxed out" in {

            val assets = Assets(
              monetary = Nil,
              propertyOrLand = Nil,
              shares = Nil,
              business = Nil,
              partnerShip = Nil,
              other = Nil,
              nonEEABusiness = Nil
            )

            navigator.addAssetRoute(assets).url mustBe
              controllers.asset.routes.WhatKindOfAssetController.onPageLoad().url
          }

          "one type maxed out" in {

            val assets = Assets(
              monetary = List.fill(MAX_MONEY_ASSETS)(money),
              propertyOrLand = Nil,
              shares = Nil,
              business = Nil,
              partnerShip = Nil,
              other = Nil,
              nonEEABusiness = Nil
            )

            navigator.addAssetRoute(assets).url mustBe
              controllers.asset.routes.WhatKindOfAssetController.onPageLoad().url
          }

          "all types maxed out bar 2" in {

            val assets = Assets(
              monetary = List.fill(MAX_MONEY_ASSETS)(money),
              propertyOrLand = List.fill(MAX_PROPERTY_OR_LAND_ASSETS)(propertyOrLand),
              shares = List.fill(MAX_SHARES_ASSETS)(share),
              business = List.fill(MAX_BUSINESS_ASSETS)(business),
              partnerShip = List.fill(MAX_PARTNERSHIP_ASSETS)(partnership),
              other = Nil,
              nonEEABusiness = Nil
            )

            navigator.addAssetRoute(assets).url mustBe
              controllers.asset.routes.WhatKindOfAssetController.onPageLoad().url
          }
        }
      }
    }

    "addAssetNowRoute" when {

      "money" must {
        "redirect to money journey" in {
          navigator.addAssetNowRoute(Money).url mustBe
            controllers.asset.money.routes.AssetMoneyValueController.onPageLoad(NormalMode).url
        }
      }

      "property or land" must {
        "redirect to property or land journey" in {
          navigator.addAssetNowRoute(PropertyOrLand).url mustBe
            controllers.asset.property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode).url
        }
      }

      "shares" must {
        "redirect to shares journey" in {
          navigator.addAssetNowRoute(Shares).url mustBe
            controllers.asset.shares.routes.SharesInAPortfolioController.onPageLoad(NormalMode).url
        }
      }

      "business" must {
        "redirect to business journey" in {
          navigator.addAssetNowRoute(Business).url mustBe
            controllers.asset.business.routes.BusinessNameController.onPageLoad(NormalMode).url
        }
      }

      "partnership" must {
        "redirect to partnership journey" in {
          navigator.addAssetNowRoute(Partnership).url mustBe
            controllers.asset.partnership.routes.PartnershipDescriptionController.onPageLoad(NormalMode).url
        }
      }

      "other" must {
        "redirect to other journey" in {
          navigator.addAssetNowRoute(Other).url mustBe
            controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode).url
        }
      }

      "non-EEA business" must {
        "redirect to non-EEA business journey" in {
          navigator.addAssetNowRoute(NonEeaBusiness).url mustBe
            controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode).url
        }
      }
    }
  }
}
