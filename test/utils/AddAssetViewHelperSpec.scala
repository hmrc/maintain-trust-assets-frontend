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

package utils

import base.SpecBase
import controllers.asset._
import models.Status._
import models.WhatKindOfAsset._
import models.{Assets, InternationalAddress, NormalMode, ShareClass, UKAddress}
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.business._
import pages.asset.money._
import pages.asset.noneeabusiness._
import pages.asset.other._
import pages.asset.partnership._
import pages.asset.property_or_land._
import pages.asset.shares._
import viewmodels.AddRow
import java.time.LocalDate

class AddAssetViewHelperSpec extends SpecBase {

  private val assetValue: Long = 4000L
  private val ukAddress: UKAddress = UKAddress("line 1", "line 2", None, None, "NE1 1NE")
  private val nonUkAddress: InternationalAddress = InternationalAddress("Line 1", "Line 2", None, "FR")
  private val date: LocalDate = LocalDate.parse("1996-02-03")

  def changeMoneyAssetRoute(index: Int): String =
    money.routes.AssetMoneyValueController.onPageLoad(NormalMode).url

  def removeAssetYesNoRoute(index: Int): String =
    "/foo"

  "AddAssetViewHelper" when {

    ".row" must {

      "generate Nil for no user answers" in {
        val rows = new AddAssetViewHelper(Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil)).rows
        rows.inProgress mustBe Nil
        rows.complete mustBe Nil
      }

      // TODO
//      "generate rows from user answers for assets in progress" in {
//
//        def changePropertyOrLandAssetRoute(index: Int): String =
//          property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode).url
//
//        def changeSharesAssetRoute(index: Int): String =
//          shares.routes.SharesInAPortfolioController.onPageLoad(NormalMode).url
//
//        def changePartnershipAssetRoute(index: Int): String =
//          partnership.routes.PartnershipDescriptionController.onPageLoad(NormalMode).url
//
//        def changeOtherAssetRoute(index: Int): String =
//          other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode).url
//
//        def changeNonEeaBusinessAssetRoute(index: Int): String =
//          noneeabusiness.routes.NameController.onPageLoad(NormalMode).url
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, Shares).success.value
//          .set(SharesInAPortfolioPage, true).success.value
//
//          .set(WhatKindOfAssetPage, Money).success.value
//
//          .set(WhatKindOfAssetPage, PropertyOrLand).success.value
//          .set(PropertyOrLandAddressYesNoPage, true).success.value
//          .set(PropertyOrLandAddressUkYesNoPage, true).success.value
//
//          .set(WhatKindOfAssetPage, PropertyOrLand).success.value
//          .set(PropertyOrLandAddressYesNoPage, false).success.value
//
//          .set(WhatKindOfAssetPage, PropertyOrLand).success.value
//
//          .set(WhatKindOfAssetPage, Other).success.value
//          .set(OtherAssetDescriptionPage, "Description").success.value
//
//          .set(WhatKindOfAssetPage, Partnership).success.value
//          .set(PartnershipDescriptionPage, "Partnership Description").success.value
//
//          .set(WhatKindOfAssetPage, NonEeaBusiness).success.value
//          .set(NamePage, "Name").success.value
//
//        val rows = new AddAssetViewHelper(userAnswers).rows
//        rows.inProgress mustBe List(
//          AddRow("No name added", typeLabel = "Shares", changeSharesAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("No value added", typeLabel = "Money", changeMoneyAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("No address added", typeLabel = "Property or land", changePropertyOrLandAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("No description added", typeLabel = "Property or land", changePropertyOrLandAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("No address or description added", typeLabel = "Property or land", changePropertyOrLandAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("Description", typeLabel = "Other", changeOtherAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("Partnership Description", typeLabel = "Partnership", changePartnershipAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("Name", typeLabel = "Non-EEA Company", changeNonEeaBusinessAssetRoute(0), removeAssetYesNoRoute(0))
//        )
//        rows.complete mustBe Nil
//      }

      // TODO
//      "generate rows from user answers for complete assets" in {
//
//        def changePropertyOrLandAssetRoute(index: Int): String =
//          property_or_land.routes.PropertyOrLandAnswerController.onPageLoad().url
//
//        def changeSharesAssetRoute(index: Int): String =
//          shares.routes.ShareAnswerController.onPageLoad().url
//
//        def changeBusinessAssetRoute(index: Int): String =
//          business.routes.BusinessAnswersController.onPageLoad().url
//
//        def changePartnershipAssetRoute(index: Int): String =
//          partnership.routes.PartnershipAnswerController.onPageLoad().url
//
//        def changeOtherAssetRoute(index: Int): String =
//          other.routes.OtherAssetAnswersController.onPageLoad().url
//
//        def changeNonEeaBusinessAssetRoute(index: Int): String =
//          noneeabusiness.routes.AnswersController.onPageLoad().url
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, Shares).success.value
//          .set(SharesInAPortfolioPage, false).success.value
//          .set(ShareCompanyNamePage, "Share Company Name").success.value
//          .set(SharesOnStockExchangePage, true).success.value
//          .set(ShareClassPage, ShareClass.Ordinary).success.value
//          .set(ShareQuantityInTrustPage, 1000L).success.value
//          .set(ShareValueInTrustPage, assetValue).success.value
//          .set(AssetStatus, Completed).success.value
//
//          .set(WhatKindOfAssetPage, Money).success.value
//          .set(AssetMoneyValuePage, assetValue).success.value
//          .set(AssetStatus, Completed).success.value
//
//          .set(WhatKindOfAssetPage, PropertyOrLand).success.value
//          .set(PropertyOrLandAddressYesNoPage, true).success.value
//          .set(PropertyOrLandAddressUkYesNoPage, true).success.value
//          .set(PropertyOrLandUKAddressPage, ukAddress).success.value
//          .set(PropertyOrLandTotalValuePage, assetValue).success.value
//          .set(TrustOwnAllThePropertyOrLandPage, true).success.value
//          .set(AssetStatus, Completed).success.value
//
//          .set(WhatKindOfAssetPage, PropertyOrLand).success.value
//          .set(PropertyOrLandAddressYesNoPage, false).success.value
//          .set(PropertyOrLandDescriptionPage, "1 hectare of land").success.value
//          .set(PropertyOrLandTotalValuePage, assetValue).success.value
//          .set(TrustOwnAllThePropertyOrLandPage, true).success.value
//          .set(AssetStatus, Completed).success.value
//
//          .set(WhatKindOfAssetPage, Other).success.value
//          .set(OtherAssetDescriptionPage, "Description").success.value
//          .set(OtherAssetValuePage, assetValue).success.value
//          .set(AssetStatus, Completed).success.value
//
//          .set(WhatKindOfAssetPage, Partnership).success.value
//          .set(PartnershipDescriptionPage, "Partnership Description").success.value
//          .set(PartnershipStartDatePage, date).success.value
//          .set(AssetStatus, Completed).success.value
//
//          .set(WhatKindOfAssetPage, Business).success.value
//          .set(BusinessNamePage, "Test").success.value
//          .set(BusinessDescriptionPage, "Test Test Test").success.value
//          .set(BusinessAddressUkYesNoPage, true).success.value
//          .set(BusinessUkAddressPage, ukAddress).success.value
//          .set(BusinessValuePage, assetValue).success.value
//          .set(AssetStatus, Completed).success.value
//
//          .set(WhatKindOfAssetPage, NonEeaBusiness).success.value
//          .set(NamePage, "Non-EEA Business Name").success.value
//          .set(InternationalAddressPage, nonUkAddress).success.value
//          .set(GoverningCountryPage, "FR").success.value
//          .set(StartDatePage, date).success.value
//          .set(AssetStatus, Completed).success.value
//
//        val rows = new AddAssetViewHelper(userAnswers).rows
//        rows.complete mustBe List(
//          AddRow("Share Company Name", typeLabel = "Shares", changeSharesAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("£4000", typeLabel = "Money", changeMoneyAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("line 1", typeLabel = "Property or land", changePropertyOrLandAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("1 hectare of land", typeLabel = "Property or land", changePropertyOrLandAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("Description", typeLabel = "Other", changeOtherAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("Partnership Description", typeLabel = "Partnership", changePartnershipAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("Test", typeLabel = "Business", changeBusinessAssetRoute(0), removeAssetYesNoRoute(0)),
//          AddRow("Non-EEA Business Name", typeLabel = "Non-EEA Company", changeNonEeaBusinessAssetRoute(0), removeAssetYesNoRoute(0))
//        )
//        rows.inProgress mustBe Nil
//      }

    }
  }
}
