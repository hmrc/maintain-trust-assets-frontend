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

package pages.asset

import models.{NonUkAddress, ShareClass, UkAddress, UserAnswers, WhatKindOfAsset}
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
  private val ukAddress: UkAddress = UkAddress(str, str, None, None, str)
  private val internationalAddress: NonUkAddress = NonUkAddress(str, str, None, str)
  private val date: LocalDate = LocalDate.parse("1996-02-03")

  "WhatKindOfAssetPage" must {

    beRetrievable[WhatKindOfAsset](WhatKindOfAssetPage(index))

    beSettable[WhatKindOfAsset](WhatKindOfAssetPage(index))

    beRemovable[WhatKindOfAsset](WhatKindOfAssetPage(index))
  }

  "remove money when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Money)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(AssetMoneyValuePage(index), num).success.value


        val result = answers.set(WhatKindOfAssetPage(index), kind).success.value

        result.get(WhatKindOfAssetPage(index)).value mustEqual kind
        result.get(AssetMoneyValuePage(index)) mustNot be(defined)

    }
  }

  "remove share portfolio data when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Shares)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(SharesInAPortfolioPage(index), true).success.value
          .set(SharePortfolioNamePage(index), str).success.value
          .set(SharePortfolioOnStockExchangePage(index), true).success.value
          .set(SharePortfolioQuantityInTrustPage(index), num).success.value
          .set(SharePortfolioValueInTrustPage(index), num).success.value
          .set(ShareCompanyNamePage(index), str).success.value
          .set(SharesOnStockExchangePage(index), false).success.value
          .set(ShareClassPage(index), ShareClass.Ordinary).success.value
          .set(ShareQuantityInTrustPage(index), num).success.value
          .set(ShareValueInTrustPage(index), num).success.value


        val result = answers.set(WhatKindOfAssetPage(index), kind).success.value

        result.get(WhatKindOfAssetPage(index)).value mustEqual kind

        result.get(SharesInAPortfolioPage(index)) mustNot be(defined)
        result.get(SharePortfolioNamePage(index)) mustNot be(defined)
        result.get(SharePortfolioOnStockExchangePage(index)) mustNot be(defined)
        result.get(SharePortfolioQuantityInTrustPage(index)) mustNot be(defined)
        result.get(SharePortfolioValueInTrustPage(index)) mustNot be(defined)
        result.get(ShareCompanyNamePage(index)) mustNot be(defined)
        result.get(SharesOnStockExchangePage(index)) mustNot be(defined)
        result.get(ShareClassPage(index)) mustNot be(defined)
        result.get(ShareQuantityInTrustPage(index)) mustNot be(defined)
        result.get(ShareValueInTrustPage(index)) mustNot be(defined)
        result.get(AssetStatus(index)) mustNot be(defined)
    }
  }

  "remove property or land when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.PropertyOrLand)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>

        val answers: UserAnswers = initial
          .set(PropertyOrLandAddressYesNoPage(index), true).success.value
          .set(PropertyOrLandAddressUkYesNoPage(index), true).success.value
          .set(PropertyOrLandUKAddressPage(index), ukAddress).success.value
          .set(PropertyOrLandInternationalAddressPage(index), internationalAddress).success.value
          .set(PropertyOrLandDescriptionPage(index), str).success.value
          .set(PropertyOrLandTotalValuePage(index), num).success.value
          .set(TrustOwnAllThePropertyOrLandPage(index), false).success.value
          .set(PropertyLandValueTrustPage(index), num).success.value


        val result: UserAnswers = answers.set(WhatKindOfAssetPage(index), kind).success.value

        result.get(WhatKindOfAssetPage(index)).value mustEqual kind

        result.get(PropertyOrLandAddressYesNoPage(index)) mustNot be(defined)
        result.get(PropertyOrLandAddressUkYesNoPage(index)) mustNot be(defined)
        result.get(PropertyOrLandUKAddressPage(index)) mustNot be(defined)
        result.get(PropertyOrLandInternationalAddressPage(index)) mustNot be(defined)
        result.get(PropertyOrLandDescriptionPage(index)) mustNot be(defined)
        result.get(PropertyOrLandTotalValuePage(index)) mustNot be(defined)
        result.get(TrustOwnAllThePropertyOrLandPage(index)) mustNot be(defined)
        result.get(PropertyLandValueTrustPage(index)) mustNot be(defined)
        result.get(AssetStatus(index)) mustNot be(defined)
    }
  }

  "remove other when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Other)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(OtherAssetDescriptionPage(index), str).success.value
          .set(OtherAssetValuePage(index), num).success.value


        val result = answers.set(WhatKindOfAssetPage(index), kind).success.value

        result.get(WhatKindOfAssetPage(index)).value mustEqual kind

        result.get(OtherAssetDescriptionPage(index)) mustNot be(defined)
        result.get(OtherAssetValuePage(index)) mustNot be(defined)

        result.get(AssetStatus(index)) mustNot be(defined)
    }
  }

  "remove partnership when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Partnership)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(PartnershipDescriptionPage(index), str).success.value
          .set(PartnershipStartDatePage(index), date).success.value


        val result = answers.set(WhatKindOfAssetPage(index), kind).success.value

        result.get(WhatKindOfAssetPage(index)).value mustEqual kind

        result.get(PartnershipDescriptionPage(index)) mustNot be(defined)
        result.get(PartnershipStartDatePage(index)) mustNot be(defined)
        result.get(AssetStatus(index)) mustNot be(defined)
    }
  }

  "remove business when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.Business)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(BusinessNamePage(index), str).success.value
          .set(BusinessDescriptionPage(index), str).success.value
          .set(BusinessUkAddressPage(index), ukAddress).success.value
          .set(BusinessInternationalAddressPage(index), internationalAddress).success.value
          .set(BusinessValuePage(index), num).success.value


        val result = answers.set(WhatKindOfAssetPage(index), kind).success.value

        result.get(WhatKindOfAssetPage(index)).value mustEqual kind

        result.get(BusinessNamePage(index)) mustNot be(defined)
        result.get(BusinessDescriptionPage(index)) mustNot be(defined)
        result.get(BusinessUkAddressPage(index)) mustNot be(defined)
        result.get(BusinessInternationalAddressPage(index)) mustNot be(defined)
        result.get(BusinessValuePage(index)) mustNot be(defined)
        result.get(AssetStatus(index)) mustNot be(defined)
    }
  }

  "remove non-EEA business when changing type of asset" in {

    val kindOfAsset = arbitrary[WhatKindOfAsset] suchThat (x => x != WhatKindOfAsset.NonEeaBusiness)

    forAll(arbitrary[UserAnswers], kindOfAsset) {
      (initial, kind) =>
        val answers: UserAnswers = initial
          .set(noneeabusiness.NamePage(index), str).success.value
          .set(noneeabusiness.NonUkAddressPage(index), internationalAddress).success.value
          .set(noneeabusiness.GoverningCountryPage(index), str).success.value
          .set(noneeabusiness.add.StartDatePage, date).success.value


        val result = answers.set(WhatKindOfAssetPage(index), kind).success.value

        result.get(WhatKindOfAssetPage(index)).value mustEqual kind

        result.get(noneeabusiness.NamePage(index)) mustNot be(defined)
        result.get(noneeabusiness.NonUkAddressPage(index)) mustNot be(defined)
        result.get(noneeabusiness.GoverningCountryPage(index)) mustNot be(defined)
        result.get(noneeabusiness.add.StartDatePage) mustNot be(defined)
        result.get(AssetStatus(index)) mustNot be(defined)
    }
  }
}
