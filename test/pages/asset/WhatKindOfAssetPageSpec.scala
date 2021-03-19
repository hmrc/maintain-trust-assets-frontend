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

    beRetrievable[WhatKindOfAsset](WhatKindOfAssetPage(0))

    beSettable[WhatKindOfAsset](WhatKindOfAssetPage(0))

    beRemovable[WhatKindOfAsset](WhatKindOfAssetPage(0))
  }

  "remove money when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Money)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(AssetMoneyValuePage(0), num).success.value
          .set(AssetStatus(0), Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage(0), kind).success.value

        result.get(WhatKindOfAssetPage(0)).value mustEqual kind
        result.get(AssetMoneyValuePage(0)) mustNot be(defined)
        result.get(AssetStatus(0)) mustNot be(defined)
    }
  }

  "remove share portfolio data when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Shares)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(SharesInAPortfolioPage(0), true).success.value
          .set(SharePortfolioNamePage(0), str).success.value
          .set(SharePortfolioOnStockExchangePage(0), true).success.value
          .set(SharePortfolioQuantityInTrustPage(0), num).success.value
          .set(SharePortfolioValueInTrustPage(0), num).success.value
          .set(ShareCompanyNamePage(0), str).success.value
          .set(SharesOnStockExchangePage(0), false).success.value
          .set(ShareClassPage(0), ShareClass.Ordinary).success.value
          .set(ShareQuantityInTrustPage(0), num).success.value
          .set(ShareValueInTrustPage(0), num).success.value
          .set(AssetStatus(0), Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage(0), kind).success.value

        result.get(WhatKindOfAssetPage(0)).value mustEqual kind

        result.get(SharesInAPortfolioPage(0)) mustNot be(defined)
        result.get(SharePortfolioNamePage(0)) mustNot be(defined)
        result.get(SharePortfolioOnStockExchangePage(0)) mustNot be(defined)
        result.get(SharePortfolioQuantityInTrustPage(0)) mustNot be(defined)
        result.get(SharePortfolioValueInTrustPage(0)) mustNot be(defined)
        result.get(ShareCompanyNamePage(0)) mustNot be(defined)
        result.get(SharesOnStockExchangePage(0)) mustNot be(defined)
        result.get(ShareClassPage(0)) mustNot be(defined)
        result.get(ShareQuantityInTrustPage(0)) mustNot be(defined)
        result.get(ShareValueInTrustPage(0)) mustNot be(defined)
        result.get(AssetStatus(0)) mustNot be(defined)
    }
  }

  "remove property or land when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.PropertyOrLand)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>

        val answers: UserAnswers = initial
          .set(PropertyOrLandAddressYesNoPage(0), true).success.value
          .set(PropertyOrLandAddressUkYesNoPage(0), true).success.value
          .set(PropertyOrLandUKAddressPage(0), ukAddress).success.value
          .set(PropertyOrLandInternationalAddressPage(0), internationalAddress).success.value
          .set(PropertyOrLandDescriptionPage(0), str).success.value
          .set(PropertyOrLandTotalValuePage(0), num).success.value
          .set(TrustOwnAllThePropertyOrLandPage(0), false).success.value
          .set(PropertyLandValueTrustPage(0), num).success.value
          .set(AssetStatus(0), Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage(0), kind).success.value

        result.get(WhatKindOfAssetPage(0)).value mustEqual kind

        result.get(PropertyOrLandAddressYesNoPage(0)) mustNot be(defined)
        result.get(PropertyOrLandAddressUkYesNoPage(0)) mustNot be(defined)
        result.get(PropertyOrLandUKAddressPage(0)) mustNot be(defined)
        result.get(PropertyOrLandInternationalAddressPage(0)) mustNot be(defined)
        result.get(PropertyOrLandDescriptionPage(0)) mustNot be(defined)
        result.get(PropertyOrLandTotalValuePage(0)) mustNot be(defined)
        result.get(TrustOwnAllThePropertyOrLandPage(0)) mustNot be(defined)
        result.get(PropertyLandValueTrustPage(0)) mustNot be(defined)
        result.get(AssetStatus(0)) mustNot be(defined)
    }
  }

  "remove other when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Other)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(OtherAssetDescriptionPage(0), str).success.value
          .set(OtherAssetValuePage(0), num).success.value
          .set(AssetStatus(0), Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage(0), kind).success.value

        result.get(WhatKindOfAssetPage(0)).value mustEqual kind

        result.get(OtherAssetDescriptionPage(0)) mustNot be(defined)
        result.get(OtherAssetValuePage(0)) mustNot be(defined)

        result.get(AssetStatus(0)) mustNot be(defined)
    }
  }

  "remove partnership when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Partnership)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(PartnershipDescriptionPage(0), str).success.value
          .set(PartnershipStartDatePage(0), date).success.value
          .set(AssetStatus(0), Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage(0), kind).success.value

        result.get(WhatKindOfAssetPage(0)).value mustEqual kind

        result.get(PartnershipDescriptionPage(0)) mustNot be(defined)
        result.get(PartnershipStartDatePage(0)) mustNot be(defined)
        result.get(AssetStatus(0)) mustNot be(defined)
    }
  }

  "remove business when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Business)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(BusinessNamePage(0), str).success.value
          .set(BusinessDescriptionPage(0), str).success.value
          .set(BusinessUkAddressPage(0), ukAddress).success.value
          .set(BusinessInternationalAddressPage(0), internationalAddress).success.value
          .set(BusinessValuePage(0), num).success.value
          .set(AssetStatus(0), Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage(0), kind).success.value

        result.get(WhatKindOfAssetPage(0)).value mustEqual kind

        result.get(BusinessNamePage(0)) mustNot be(defined)
        result.get(BusinessDescriptionPage(0)) mustNot be(defined)
        result.get(BusinessUkAddressPage(0)) mustNot be(defined)
        result.get(BusinessInternationalAddressPage(0)) mustNot be(defined)
        result.get(BusinessValuePage(0)) mustNot be(defined)
        result.get(AssetStatus(0)) mustNot be(defined)
    }
  }

  "remove non-EEA business when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.NonEeaBusiness)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(noneeabusiness.NamePage(0), str).success.value
          .set(noneeabusiness.InternationalAddressPage(0), internationalAddress).success.value
          .set(noneeabusiness.GoverningCountryPage(0), str).success.value
          .set(noneeabusiness.StartDatePage(0), date).success.value
          .set(AssetStatus(0), Status.Completed).success.value

        val result = answers.set(WhatKindOfAssetPage(0), kind).success.value

        result.get(WhatKindOfAssetPage(0)).value mustEqual kind

        result.get(noneeabusiness.NamePage(0)) mustNot be(defined)
        result.get(noneeabusiness.InternationalAddressPage(0)) mustNot be(defined)
        result.get(noneeabusiness.GoverningCountryPage(0)) mustNot be(defined)
        result.get(noneeabusiness.StartDatePage(0)) mustNot be(defined)
        result.get(AssetStatus(0)) mustNot be(defined)
    }
  }
}
