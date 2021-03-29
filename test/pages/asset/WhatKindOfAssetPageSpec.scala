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

package pages.asset

import models.{InternationalAddress, ShareClass, Status, UKAddress, UserAnswers, WhatKindOfAsset}
import org.scalacheck.Arbitrary.arbitrary
import pages.AssetStatus
import pages.asset.business._
import pages.asset.money._
import pages.asset.other._
import pages.asset.partnership._
import pages.asset.property_or_land._
import pages.asset.shares._
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class WhatKindOfAssetPageSpec extends PageBehaviours {

  private val str: String = "string"
  private val num: Long = 20L
  private val ukAddress: UKAddress = UKAddress(str, str, None, None, str)
  private val internationalAddress: InternationalAddress = InternationalAddress(str, str, None, str)
  private val date: LocalDate = LocalDate.parse("1996-02-03")

  "WhatKindOfAssetPage" must {

    beRetrievable[WhatKindOfAsset](WhatKindOfAssetPage)

    beSettable[WhatKindOfAsset](WhatKindOfAssetPage)

    beRemovable[WhatKindOfAsset](WhatKindOfAssetPage)
  }

  "remove money when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Money)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(AssetMoneyValuePage, num).success.value
          .set(AssetStatus, Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage, kind).success.value

        result.get(WhatKindOfAssetPage).value mustEqual kind
        result.get(AssetMoneyValuePage) mustNot be(defined)
        result.get(AssetStatus) mustNot be(defined)
    }
  }

  "remove share portfolio data when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Shares)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(SharesInAPortfolioPage, true).success.value
          .set(SharePortfolioNamePage, str).success.value
          .set(SharePortfolioOnStockExchangePage, true).success.value
          .set(SharePortfolioQuantityInTrustPage, num).success.value
          .set(SharePortfolioValueInTrustPage, num).success.value
          .set(ShareCompanyNamePage, str).success.value
          .set(SharesOnStockExchangePage, false).success.value
          .set(ShareClassPage, ShareClass.Ordinary).success.value
          .set(ShareQuantityInTrustPage, num).success.value
          .set(ShareValueInTrustPage, num).success.value
          .set(AssetStatus, Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage, kind).success.value

        result.get(WhatKindOfAssetPage).value mustEqual kind

        result.get(SharesInAPortfolioPage) mustNot be(defined)
        result.get(SharePortfolioNamePage) mustNot be(defined)
        result.get(SharePortfolioOnStockExchangePage) mustNot be(defined)
        result.get(SharePortfolioQuantityInTrustPage) mustNot be(defined)
        result.get(SharePortfolioValueInTrustPage) mustNot be(defined)
        result.get(ShareCompanyNamePage) mustNot be(defined)
        result.get(SharesOnStockExchangePage) mustNot be(defined)
        result.get(ShareClassPage) mustNot be(defined)
        result.get(ShareQuantityInTrustPage) mustNot be(defined)
        result.get(ShareValueInTrustPage) mustNot be(defined)
        result.get(AssetStatus) mustNot be(defined)
    }
  }

  "remove property or land when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.PropertyOrLand)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>

        val answers: UserAnswers = initial
          .set(PropertyOrLandAddressYesNoPage, true).success.value
          .set(PropertyOrLandAddressUkYesNoPage, true).success.value
          .set(PropertyOrLandUKAddressPage, ukAddress).success.value
          .set(PropertyOrLandInternationalAddressPage, internationalAddress).success.value
          .set(PropertyOrLandDescriptionPage, str).success.value
          .set(PropertyOrLandTotalValuePage, num).success.value
          .set(TrustOwnAllThePropertyOrLandPage, false).success.value
          .set(PropertyLandValueTrustPage, num).success.value
          .set(AssetStatus, Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage, kind).success.value

        result.get(WhatKindOfAssetPage).value mustEqual kind

        result.get(PropertyOrLandAddressYesNoPage) mustNot be(defined)
        result.get(PropertyOrLandAddressUkYesNoPage) mustNot be(defined)
        result.get(PropertyOrLandUKAddressPage) mustNot be(defined)
        result.get(PropertyOrLandInternationalAddressPage) mustNot be(defined)
        result.get(PropertyOrLandDescriptionPage) mustNot be(defined)
        result.get(PropertyOrLandTotalValuePage) mustNot be(defined)
        result.get(TrustOwnAllThePropertyOrLandPage) mustNot be(defined)
        result.get(PropertyLandValueTrustPage) mustNot be(defined)
        result.get(AssetStatus) mustNot be(defined)
    }
  }

  "remove other when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Other)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(OtherAssetDescriptionPage, str).success.value
          .set(OtherAssetValuePage, num).success.value
          .set(AssetStatus, Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage, kind).success.value

        result.get(WhatKindOfAssetPage).value mustEqual kind

        result.get(OtherAssetDescriptionPage) mustNot be(defined)
        result.get(OtherAssetValuePage) mustNot be(defined)

        result.get(AssetStatus) mustNot be(defined)
    }
  }

  "remove partnership when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Partnership)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(PartnershipDescriptionPage, str).success.value
          .set(PartnershipStartDatePage, date).success.value
          .set(AssetStatus, Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage, kind).success.value

        result.get(WhatKindOfAssetPage).value mustEqual kind

        result.get(PartnershipDescriptionPage) mustNot be(defined)
        result.get(PartnershipStartDatePage) mustNot be(defined)
        result.get(AssetStatus) mustNot be(defined)
    }
  }

  "remove business when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Business)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(BusinessNamePage, str).success.value
          .set(BusinessDescriptionPage, str).success.value
          .set(BusinessUkAddressPage, ukAddress).success.value
          .set(BusinessInternationalAddressPage, internationalAddress).success.value
          .set(BusinessValuePage, num).success.value
          .set(AssetStatus, Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage, kind).success.value

        result.get(WhatKindOfAssetPage).value mustEqual kind

        result.get(BusinessNamePage) mustNot be(defined)
        result.get(BusinessDescriptionPage) mustNot be(defined)
        result.get(BusinessUkAddressPage) mustNot be(defined)
        result.get(BusinessInternationalAddressPage) mustNot be(defined)
        result.get(BusinessValuePage) mustNot be(defined)
        result.get(AssetStatus) mustNot be(defined)
    }
  }

  "remove non-EEA business when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.NonEeaBusiness)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(noneeabusiness.NamePage, str).success.value
          .set(noneeabusiness.InternationalAddressPage, internationalAddress).success.value
          .set(noneeabusiness.GoverningCountryPage, str).success.value
          .set(noneeabusiness.StartDatePage, date).success.value
          .set(AssetStatus, Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage, kind).success.value

        result.get(WhatKindOfAssetPage).value mustEqual kind

        result.get(noneeabusiness.NamePage) mustNot be(defined)
        result.get(noneeabusiness.InternationalAddressPage) mustNot be(defined)
        result.get(noneeabusiness.GoverningCountryPage) mustNot be(defined)
        result.get(noneeabusiness.StartDatePage) mustNot be(defined)
        result.get(AssetStatus) mustNot be(defined)
    }
  }
}
