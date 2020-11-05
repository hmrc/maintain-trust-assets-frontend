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

package mapping

import base.SpecBase
import generators.Generators
import models.Status.Completed
import models._
import org.scalatest.{MustMatchers, OptionValues}
import pages.AssetStatus
import pages.asset._
import pages.asset.money._
import pages.asset.property_or_land._
import pages.asset.shares._

class AssetMapperSpec extends SpecBase with MustMatchers
  with OptionValues with Generators {

  val assetMapper: Mapping[Assets] = injector.instanceOf[AssetMapper]

  private val shareAssetValue: Long = 999999999999L
  private val moneyAssetValue: Long = 2000L
  private val propertyOrLandAssetTotalValue: Long = 1000L
  private val propertyOrLandAssetTrustValue: Long = 750L

  "AssetMapper" when {

    "user answers is empty" must {

      "not be able to create Assets" in {

        val userAnswers = emptyUserAnswers

        assetMapper.build(userAnswers) mustNot be(defined)
      }
    }

    "when user answers is not empty " must {

      "be able to create Assets for money" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), WhatKindOfAsset.Money).success.value
          .set(AssetMoneyValuePage(0), moneyAssetValue).success.value
          .set(AssetStatus(0), Completed).success.value


        val expected = Some(Assets(Some(List(AssetMonetaryAmount(moneyAssetValue))), None, None, None, None, None))

        assetMapper.build(userAnswers) mustBe expected
      }

      "be able to create Assets for shares" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), WhatKindOfAsset.Shares).success.value
          .set(SharesInAPortfolioPage(0), true).success.value
          .set(SharePortfolioNamePage(0), "Portfolio").success.value
          .set(SharePortfolioQuantityInTrustPage(0), "30").success.value
          .set(SharePortfolioValueInTrustPage(0), shareAssetValue).success.value
          .set(SharePortfolioOnStockExchangePage(0), false).success.value
          .set(AssetStatus(0), Completed).success.value


        val expected = Some(Assets(None,None,Some(List(SharesType("30", "Portfolio", "Other", "Unquoted", shareAssetValue))), None, None, None))

        assetMapper.build(userAnswers) mustBe expected
      }

      "be able to create Assets for both shares and money" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), WhatKindOfAsset.Shares).success.value
          .set(SharesInAPortfolioPage(0), true).success.value
          .set(SharePortfolioNamePage(0), "Portfolio").success.value
          .set(SharePortfolioQuantityInTrustPage(0), "30").success.value
          .set(SharePortfolioValueInTrustPage(0), shareAssetValue).success.value
          .set(SharePortfolioOnStockExchangePage(0), false).success.value
          .set(AssetStatus(0), Completed).success.value
          .set(WhatKindOfAssetPage(1), WhatKindOfAsset.Money).success.value
          .set(AssetMoneyValuePage(1), moneyAssetValue).success.value
          .set(AssetStatus(1), Completed).success.value


        val expected = Some(Assets(
          Some(List(AssetMonetaryAmount(moneyAssetValue))),
          None,
          Some(List(SharesType("30", "Portfolio", "Other", "Unquoted", shareAssetValue))),
          None,
          None,
          None
        ))

        assetMapper.build(userAnswers) mustBe expected
      }

      "be able to create Assets for both shares, money and property or land" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(0), WhatKindOfAsset.Shares).success.value
          .set(SharesInAPortfolioPage(0), true).success.value
          .set(SharePortfolioNamePage(0), "Portfolio").success.value
          .set(SharePortfolioQuantityInTrustPage(0), "30").success.value
          .set(SharePortfolioValueInTrustPage(0), shareAssetValue).success.value
          .set(SharePortfolioOnStockExchangePage(0), false).success.value
          .set(AssetStatus(0), Completed).success.value
          .set(WhatKindOfAssetPage(1), WhatKindOfAsset.Money).success.value
          .set(AssetMoneyValuePage(1), moneyAssetValue).success.value
          .set(AssetStatus(1), Completed).success.value
          .set(WhatKindOfAssetPage(2), WhatKindOfAsset.PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage(2), true).success.value
          .set(PropertyOrLandAddressUkYesNoPage(2), true).success.value
          .set(PropertyOrLandUKAddressPage(2), UKAddress("26", "Grangetown", Some("Tyne and Wear"), Some("Newcastle"), "Z99 2YY")).success.value
          .set(PropertyOrLandTotalValuePage(2), propertyOrLandAssetTotalValue).success.value
          .set(TrustOwnAllThePropertyOrLandPage(2), false).success.value
          .set(PropertyLandValueTrustPage(2), propertyOrLandAssetTrustValue).success.value


        val expected = Some(Assets(
          Some(List(AssetMonetaryAmount(moneyAssetValue))),
          Some(List(PropertyLandType(None, Some(AddressType("26", "Grangetown", Some("Tyne and Wear"), Some("Newcastle"), Some("Z99 2YY"), "GB")), propertyOrLandAssetTotalValue, propertyOrLandAssetTrustValue))),
          Some(List(SharesType("30", "Portfolio", "Other", "Unquoted", shareAssetValue))),
          None,
          None,
          None
        ))

        assetMapper.build(userAnswers) mustBe expected
      }
    }
  }
}
