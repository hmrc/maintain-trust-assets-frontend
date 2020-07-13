/*
 * Copyright 2020 HM Revenue & Customs
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

package utils

import java.time.{LocalDate, ZoneOffset}

import base.SpecBase
import models.{NormalMode, ShareClass, UKAddress}
import viewmodels.AddRow
import controllers.asset._
import models.Status._
import models.WhatKindOfAsset._
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.money._
import pages.asset.property_or_land._
import pages.asset.shares._
import pages.asset.business._
import pages.asset.partnership._
import pages.asset.other._

class AddAssetViewHelperSpec extends SpecBase {

  "AddAssetViewHelper" when {

    ".row" must {

      "generate Nil for no user answers" in {
        val rows = new AddAssetViewHelper(emptyUserAnswers, NormalMode, fakeDraftId).rows
        rows.inProgress mustBe Nil
        rows.complete mustBe Nil
      }

      "generate rows from user answers for assets in progress" in {

        def propertyOrLandUkAddressRoute(index: Int): String =
          property_or_land.routes.PropertyOrLandUKAddressController.onPageLoad(NormalMode, index, fakeDraftId).url

        def propertyOrLandDescriptionRoute(index: Int): String =
          property_or_land.routes.PropertyOrLandDescriptionController.onPageLoad(NormalMode, index, fakeDraftId).url

        def propertyOrLandAddressYesNoRoute(index: Int): String =
          property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode, index, fakeDraftId).url

        def sharesRoute(index: Int): String =
          shares.routes.SharePortfolioNameController.onPageLoad(NormalMode, index, fakeDraftId).url

        def partnershipRoute(index: Int): String =
          partnership.routes.PartnershipStartDateController.onPageLoad(NormalMode, index, fakeDraftId).url

        def otherRoute(index: Int): String =
          other.routes.OtherAssetValueController.onPageLoad(NormalMode, index, fakeDraftId).url

        def removeAssetYesNoRoute(index: Int): String =
          routes.RemoveAssetYesNoController.onPageLoad(index, fakeDraftId).url

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), Shares).success.value
          .set(SharesInAPortfolioPage(0), true).success.value
          .set(WhatKindOfAssetPage(1), Money).success.value
          .set(WhatKindOfAssetPage(2), PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage(2), true).success.value
          .set(PropertyOrLandAddressUkYesNoPage(2), true).success.value
          .set(WhatKindOfAssetPage(3), PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage(3), false).success.value
          .set(WhatKindOfAssetPage(4), PropertyOrLand).success.value
          .set(WhatKindOfAssetPage(5), Other).success.value
          .set(OtherAssetDescriptionPage(5), "Description").success.value
          .set(WhatKindOfAssetPage(6), Partnership).success.value
          .set(PartnershipDescriptionPage(6), "Partnership Description").success.value

        val rows = new AddAssetViewHelper(userAnswers, NormalMode, fakeDraftId).rows
        rows.inProgress mustBe List(
          AddRow("No name added", typeLabel = "Shares", sharesRoute(0), removeAssetYesNoRoute(0)),
          AddRow("No address added", typeLabel = "Property or Land", propertyOrLandUkAddressRoute(2), removeAssetYesNoRoute(2)),
          AddRow("No description added", typeLabel = "Property or Land", propertyOrLandDescriptionRoute(3), removeAssetYesNoRoute(3)),
          AddRow("No address or description added", typeLabel = "Property or Land", propertyOrLandAddressYesNoRoute(4), removeAssetYesNoRoute(4)),
          AddRow("Description", typeLabel = "Other", otherRoute(5), removeAssetYesNoRoute(5)),
          AddRow("Partnership Description", typeLabel = "Partnership", partnershipRoute(6), removeAssetYesNoRoute(6))
        )
        rows.complete mustBe Nil
      }

      "generate rows from user answers for complete assets" in {

        def moneyRoute(index: Int): String =
          money.routes.AssetMoneyValueController.onPageLoad(NormalMode, index, fakeDraftId).url

        def propertyOrLandRoute(index: Int): String =
          property_or_land.routes.PropertyOrLandAnswerController.onPageLoad(index, fakeDraftId).url

        def sharesRoute(index: Int): String =
          shares.routes.ShareAnswerController.onPageLoad(index, fakeDraftId).url

        def businessRoute(index: Int): String =
          business.routes.BusinessAnswersController.onPageLoad(index, fakeDraftId).url

        def partnershipRoute(index: Int): String =
          partnership.routes.PartnershipAnswerController.onPageLoad(index, fakeDraftId).url

        def otherRoute(index: Int): String =
          other.routes.OtherAssetAnswersController.onPageLoad(index, fakeDraftId).url

        def removeAssetYesNoRoute(index: Int): String =
          routes.RemoveAssetYesNoController.onPageLoad(index, fakeDraftId).url

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), Shares).success.value
          .set(SharesInAPortfolioPage(0), false).success.value
          .set(ShareCompanyNamePage(0), "Share Company Name").success.value
          .set(SharesOnStockExchangePage(0), true).success.value
          .set(ShareClassPage(0), ShareClass.Ordinary).success.value
          .set(ShareQuantityInTrustPage(0), "1000").success.value
          .set(ShareValueInTrustPage(0), "10").success.value
          .set(AssetStatus(0), Completed).success.value
          .set(WhatKindOfAssetPage(1), Money).success.value
          .set(AssetMoneyValuePage(1), "200").success.value
          .set(AssetStatus(1), Completed).success.value
          .set(WhatKindOfAssetPage(2), PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage(2), true).success.value
          .set(PropertyOrLandAddressUkYesNoPage(2), true).success.value
          .set(PropertyOrLandUKAddressPage(2), UKAddress("line 1", "line 2", None, None, "NE1 1NE")).success.value
          .set(PropertyOrLandTotalValuePage(2), "100").success.value
          .set(TrustOwnAllThePropertyOrLandPage(2), true).success.value
          .set(AssetStatus(2), Completed).success.value
          .set(WhatKindOfAssetPage(3), PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage(3), false).success.value
          .set(PropertyOrLandDescriptionPage(3), "1 hectare of land").success.value
          .set(PropertyOrLandTotalValuePage(3), "100").success.value
          .set(TrustOwnAllThePropertyOrLandPage(3), true).success.value
          .set(AssetStatus(3), Completed).success.value
          .set(WhatKindOfAssetPage(4), Other).success.value
          .set(OtherAssetDescriptionPage(4), "Description").success.value
          .set(OtherAssetValuePage(4), "4000").success.value
          .set(AssetStatus(4), Completed).success.value
          .set(WhatKindOfAssetPage(5), Partnership).success.value
          .set(PartnershipDescriptionPage(5), "Partnership Description").success.value
          .set(PartnershipStartDatePage(5), LocalDate.now(ZoneOffset.UTC)).success.value
          .set(AssetStatus(5), Completed).success.value
          .set(WhatKindOfAssetPage(6), Business).success.value
          .set(BusinessNamePage(6), "Test").success.value
          .set(BusinessDescriptionPage(6), "Test Test Test").success.value
          .set(BusinessAddressUkYesNoPage(6), true).success.value
          .set(BusinessUkAddressPage(6), UKAddress("Test Line 1", "Test Line 2", None, None, "NE11NE")).success.value
          .set(BusinessValuePage(6), "12").success.value
          .set(AssetStatus(6), Completed).success.value

        val rows = new AddAssetViewHelper(userAnswers, NormalMode, fakeDraftId).rows
        rows.complete mustBe List(
          AddRow("Share Company Name", typeLabel = "Shares", sharesRoute(0), removeAssetYesNoRoute(0)),
          AddRow("£200", typeLabel = "Money", moneyRoute(1), removeAssetYesNoRoute(1)),
          AddRow("line 1", typeLabel = "Property or Land", propertyOrLandRoute(2), removeAssetYesNoRoute(2)),
          AddRow("1 hectare of land", typeLabel = "Property or Land", propertyOrLandRoute(3), removeAssetYesNoRoute(3)),
          AddRow("Description", typeLabel = "Other", otherRoute(4), removeAssetYesNoRoute(4)),
          AddRow("Partnership Description", typeLabel = "Partnership", partnershipRoute(5), removeAssetYesNoRoute(5)),
          AddRow("Test", typeLabel = "Business", businessRoute(6), removeAssetYesNoRoute(6))
        )
        rows.inProgress mustBe Nil
      }

    }
  }
}
