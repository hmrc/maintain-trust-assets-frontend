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

  val assetMapper: AssetMapper = injector.instanceOf[AssetMapper]

  private val shareAssetValue: Long = 999999999999L
  private val moneyAssetValue: Long = 2000L
  private val propertyOrLandAssetTotalValue: Long = 1000L
  private val propertyOrLandAssetTrustValue: Long = 750L
  private val quantity: Long = 30L

  "AssetMapper" when {

    "user answers is empty" must {

      "not be able to create Assets" in {

        val userAnswers = emptyUserAnswers

        assetMapper.build(userAnswers) mustNot be(defined)
      }
    }

    // TODO
//    "when user answers is not empty " must {
//
//      "be able to create Assets for money" in {
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Money).success.value
//          .set(AssetMoneyValuePage, moneyAssetValue).success.value
//          .set(AssetStatus, Completed).success.value
//
//        val expected = Some(Assets(
//          Some(List(AssetMonetaryAmount(moneyAssetValue))),
//          None,
//          None,
//          None,
//          None,
//          None,
//          None
//        ))
//
//        assetMapper.build(userAnswers) mustBe expected
//      }
//
//      "be able to create Assets for shares" in {
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Shares).success.value
//          .set(SharesInAPortfolioPage, true).success.value
//          .set(SharePortfolioNamePage, "Portfolio").success.value
//          .set(SharePortfolioQuantityInTrustPage, quantity).success.value
//          .set(SharePortfolioValueInTrustPage, shareAssetValue).success.value
//          .set(SharePortfolioOnStockExchangePage, false).success.value
//          .set(AssetStatus, Completed).success.value
//
//        val expected = Some(Assets(
//          None,
//          None,
//          Some(List(SharesType(quantity.toString, "Portfolio", "Other", "Unquoted", shareAssetValue))),
//          None,
//          None,
//          None,
//          None
//        ))
//
//        assetMapper.build(userAnswers) mustBe expected
//      }
//
//      "be able to create Assets for both shares and money" in {
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Shares).success.value
//          .set(SharesInAPortfolioPage, true).success.value
//          .set(SharePortfolioNamePage, "Portfolio").success.value
//          .set(SharePortfolioQuantityInTrustPage, quantity).success.value
//          .set(SharePortfolioValueInTrustPage, shareAssetValue).success.value
//          .set(SharePortfolioOnStockExchangePage, false).success.value
//          .set(AssetStatus, Completed).success.value
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Money).success.value
//          .set(AssetMoneyValuePage, moneyAssetValue).success.value
//          .set(AssetStatus, Completed).success.value
//
//        val expected = Some(Assets(
//          Some(List(AssetMonetaryAmount(moneyAssetValue))),
//          None,
//          Some(List(SharesType(quantity.toString, "Portfolio", "Other", "Unquoted", shareAssetValue))),
//          None,
//          None,
//          None,
//          None
//        ))
//
//        assetMapper.build(userAnswers) mustBe expected
//      }
//
//      "be able to create Assets for both shares, money and property or land" in {
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Shares).success.value
//          .set(SharesInAPortfolioPage, true).success.value
//          .set(SharePortfolioNamePage, "Portfolio").success.value
//          .set(SharePortfolioQuantityInTrustPage, quantity).success.value
//          .set(SharePortfolioValueInTrustPage, shareAssetValue).success.value
//          .set(SharePortfolioOnStockExchangePage, false).success.value
//          .set(AssetStatus, Completed).success.value
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Money).success.value
//          .set(AssetMoneyValuePage, moneyAssetValue).success.value
//          .set(AssetStatus, Completed).success.value
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.PropertyOrLand).success.value
//          .set(PropertyOrLandAddressYesNoPage, true).success.value
//          .set(PropertyOrLandAddressUkYesNoPage, true).success.value
//          .set(PropertyOrLandUKAddressPage, UKAddress("26", "Grangetown", Some("Tyne and Wear"), Some("Newcastle"), "Z99 2YY")).success.value
//          .set(PropertyOrLandTotalValuePage, propertyOrLandAssetTotalValue).success.value
//          .set(TrustOwnAllThePropertyOrLandPage, false).success.value
//          .set(PropertyLandValueTrustPage, propertyOrLandAssetTrustValue).success.value
//
//        val expected = Some(Assets(
//          Some(List(AssetMonetaryAmount(moneyAssetValue))),
//          Some(List(PropertyLandType(None, Some(AddressType("26", "Grangetown", Some("Tyne and Wear"), Some("Newcastle"), Some("Z99 2YY"), "GB")), propertyOrLandAssetTotalValue, propertyOrLandAssetTrustValue))),
//          Some(List(SharesType(quantity.toString, "Portfolio", "Other", "Unquoted", shareAssetValue))),
//          None,
//          None,
//          None,
//          None
//        ))
//
//        assetMapper.build(userAnswers) mustBe expected
//      }
//    }
  }
}
