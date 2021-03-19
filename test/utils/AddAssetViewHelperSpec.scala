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
import models.{InternationalAddress, ShareClass, UKAddress}
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
    money.routes.AssetMoneyValueController.onPageLoad(index, fakeDraftId).url

  def removeAssetYesNoRoute(index: Int): String =
    routes.RemoveAssetYesNoController.onPageLoad(index, fakeDraftId).url

  "AddAssetViewHelper" when {

    ".row" must {

      "generate Nil for no user answers" in {
        val rows = new AddAssetViewHelper(emptyUserAnswers, fakeDraftId).rows
        rows.inProgress mustBe Nil
        rows.complete mustBe Nil
      }

      "generate rows from user answers for assets in progress" in {

        def changePropertyOrLandAssetRoute(index: Int): String =
          property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(index, fakeDraftId).url

        def changeSharesAssetRoute(index: Int): String =
          shares.routes.SharesInAPortfolioController.onPageLoad(index, fakeDraftId).url

        def changePartnershipAssetRoute(index: Int): String =
          partnership.routes.PartnershipDescriptionController.onPageLoad(index, fakeDraftId).url

        def changeOtherAssetRoute(index: Int): String =
          other.routes.OtherAssetDescriptionController.onPageLoad(index, fakeDraftId).url

        def changeNonEeaBusinessAssetRoute(index: Int): String =
          noneeabusiness.routes.NameController.onPageLoad(index, fakeDraftId).url

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

          .set(WhatKindOfAssetPage(7), NonEeaBusiness).success.value
          .set(NamePage(7), "Name").success.value

        val rows = new AddAssetViewHelper(userAnswers, fakeDraftId).rows
        rows.inProgress mustBe List(
          AddRow("No name added", typeLabel = "Shares", changeSharesAssetRoute(0), removeAssetYesNoRoute(0)),
          AddRow("No value added", typeLabel = "Money", changeMoneyAssetRoute(1), removeAssetYesNoRoute(1)),
          AddRow("No address added", typeLabel = "Property or land", changePropertyOrLandAssetRoute(2), removeAssetYesNoRoute(2)),
          AddRow("No description added", typeLabel = "Property or land", changePropertyOrLandAssetRoute(3), removeAssetYesNoRoute(3)),
          AddRow("No address or description added", typeLabel = "Property or land", changePropertyOrLandAssetRoute(4), removeAssetYesNoRoute(4)),
          AddRow("Description", typeLabel = "Other", changeOtherAssetRoute(5), removeAssetYesNoRoute(5)),
          AddRow("Partnership Description", typeLabel = "Partnership", changePartnershipAssetRoute(6), removeAssetYesNoRoute(6)),
          AddRow("Name", typeLabel = "Non-EEA Company", changeNonEeaBusinessAssetRoute(7), removeAssetYesNoRoute(7))
        )
        rows.complete mustBe Nil
      }

      "generate rows from user answers for complete assets" in {

        def changePropertyOrLandAssetRoute(index: Int): String =
          property_or_land.routes.PropertyOrLandAnswerController.onPageLoad(index, fakeDraftId).url

        def changeSharesAssetRoute(index: Int): String =
          shares.routes.ShareAnswerController.onPageLoad(index, fakeDraftId).url

        def changeBusinessAssetRoute(index: Int): String =
          business.routes.BusinessAnswersController.onPageLoad(index, fakeDraftId).url

        def changePartnershipAssetRoute(index: Int): String =
          partnership.routes.PartnershipAnswerController.onPageLoad(index, fakeDraftId).url

        def changeOtherAssetRoute(index: Int): String =
          other.routes.OtherAssetAnswersController.onPageLoad(index, fakeDraftId).url

        def changeNonEeaBusinessAssetRoute(index: Int): String =
          noneeabusiness.routes.AnswersController.onPageLoad(index, fakeDraftId).url

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), Shares).success.value
          .set(SharesInAPortfolioPage(0), false).success.value
          .set(ShareCompanyNamePage(0), "Share Company Name").success.value
          .set(SharesOnStockExchangePage(0), true).success.value
          .set(ShareClassPage(0), ShareClass.Ordinary).success.value
          .set(ShareQuantityInTrustPage(0), 1000L).success.value
          .set(ShareValueInTrustPage(0), assetValue).success.value
          .set(AssetStatus(0), Completed).success.value

          .set(WhatKindOfAssetPage(1), Money).success.value
          .set(AssetMoneyValuePage(1), assetValue).success.value
          .set(AssetStatus(1), Completed).success.value

          .set(WhatKindOfAssetPage(2), PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage(2), true).success.value
          .set(PropertyOrLandAddressUkYesNoPage(2), true).success.value
          .set(PropertyOrLandUKAddressPage(2), ukAddress).success.value
          .set(PropertyOrLandTotalValuePage(2), assetValue).success.value
          .set(TrustOwnAllThePropertyOrLandPage(2), true).success.value
          .set(AssetStatus(2), Completed).success.value

          .set(WhatKindOfAssetPage(3), PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage(3), false).success.value
          .set(PropertyOrLandDescriptionPage(3), "1 hectare of land").success.value
          .set(PropertyOrLandTotalValuePage(3), assetValue).success.value
          .set(TrustOwnAllThePropertyOrLandPage(3), true).success.value
          .set(AssetStatus(3), Completed).success.value

          .set(WhatKindOfAssetPage(4), Other).success.value
          .set(OtherAssetDescriptionPage(4), "Description").success.value
          .set(OtherAssetValuePage(4), assetValue).success.value
          .set(AssetStatus(4), Completed).success.value

          .set(WhatKindOfAssetPage(5), Partnership).success.value
          .set(PartnershipDescriptionPage(5), "Partnership Description").success.value
          .set(PartnershipStartDatePage(5), date).success.value
          .set(AssetStatus(5), Completed).success.value

          .set(WhatKindOfAssetPage(6), Business).success.value
          .set(BusinessNamePage(6), "Test").success.value
          .set(BusinessDescriptionPage(6), "Test Test Test").success.value
          .set(BusinessAddressUkYesNoPage(6), true).success.value
          .set(BusinessUkAddressPage(6), ukAddress).success.value
          .set(BusinessValuePage(6), assetValue).success.value
          .set(AssetStatus(6), Completed).success.value

          .set(WhatKindOfAssetPage(7), NonEeaBusiness).success.value
          .set(NamePage(7), "Non-EEA Business Name").success.value
          .set(InternationalAddressPage(7), nonUkAddress).success.value
          .set(GoverningCountryPage(7), "FR").success.value
          .set(StartDatePage(7), date).success.value
          .set(AssetStatus(7), Completed).success.value

        val rows = new AddAssetViewHelper(userAnswers, fakeDraftId).rows
        rows.complete mustBe List(
          AddRow("Share Company Name", typeLabel = "Shares", changeSharesAssetRoute(0), removeAssetYesNoRoute(0)),
          AddRow("£4000", typeLabel = "Money", changeMoneyAssetRoute(1), removeAssetYesNoRoute(1)),
          AddRow("line 1", typeLabel = "Property or land", changePropertyOrLandAssetRoute(2), removeAssetYesNoRoute(2)),
          AddRow("1 hectare of land", typeLabel = "Property or land", changePropertyOrLandAssetRoute(3), removeAssetYesNoRoute(3)),
          AddRow("Description", typeLabel = "Other", changeOtherAssetRoute(4), removeAssetYesNoRoute(4)),
          AddRow("Partnership Description", typeLabel = "Partnership", changePartnershipAssetRoute(5), removeAssetYesNoRoute(5)),
          AddRow("Test", typeLabel = "Business", changeBusinessAssetRoute(6), removeAssetYesNoRoute(6)),
          AddRow("Non-EEA Business Name", typeLabel = "Non-EEA Company", changeNonEeaBusinessAssetRoute(7), removeAssetYesNoRoute(7))
        )
        rows.inProgress mustBe Nil
      }

    }
  }
}
