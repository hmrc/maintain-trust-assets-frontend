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
import models.Status.{Completed, InProgress}
import models.{ShareClass, SharesType, WhatKindOfAsset}
import org.scalatest.{MustMatchers, OptionValues}
import pages.AssetStatus
import pages.asset._
import pages.asset.shares._

class ShareAssetMapperSpec extends SpecBase with MustMatchers
  with OptionValues with Generators {

  val shareAssetMapper: ShareAssetMapper = injector.instanceOf[ShareAssetMapper]

  private val assetValue: Long = 300L
  private val quantity: Long = 20L

  "ShareAssetMapper" must {
    "not be able to create a share asset when missing values in user answers" in {

      val userAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage, WhatKindOfAsset.Shares).success.value
        .set(SharesInAPortfolioPage, true).success.value
        .set(AssetStatus, InProgress).success.value

      shareAssetMapper.build(userAnswers) mustNot be(defined)
    }

    "non-portfolio" must {

      "be able to create a Share Asset" in {
        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Shares).success.value
          .set(SharesInAPortfolioPage, false).success.value
          .set(ShareCompanyNamePage, "Non-Portfolio").success.value
          .set(ShareQuantityInTrustPage, quantity).success.value
          .set(ShareValueInTrustPage, assetValue).success.value
          .set(SharesOnStockExchangePage, true).success.value
          .set(ShareClassPage, ShareClass.Deferred).success.value
          .set(AssetStatus, Completed).success.value

        shareAssetMapper.build(userAnswers).value mustBe
            List(
              SharesType(
                numberOfShares = quantity.toString,
                orgName = "Non-Portfolio",
                shareClass = "Deferred ordinary shares",
                typeOfShare = "Quoted",
                value = assetValue
              )
            )
      }

      "be able to create multiple Share Assets" in {
        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Shares).success.value
          .set(SharesInAPortfolioPage, false).success.value
          .set(ShareCompanyNamePage, "Non-Portfolio").success.value
          .set(ShareQuantityInTrustPage, quantity).success.value
          .set(ShareValueInTrustPage, assetValue).success.value
          .set(SharesOnStockExchangePage, true).success.value
          .set(ShareClassPage, ShareClass.Deferred).success.value
          .set(AssetStatus, Completed).success.value
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Shares).success.value
          .set(SharesInAPortfolioPage, false).success.value
          .set(ShareCompanyNamePage, "Non-Portfolio").success.value
          .set(ShareQuantityInTrustPage, quantity).success.value
          .set(ShareValueInTrustPage, assetValue).success.value
          .set(SharesOnStockExchangePage, true).success.value
          .set(ShareClassPage, ShareClass.Deferred).success.value
          .set(AssetStatus, Completed).success.value


        shareAssetMapper.build(userAnswers).value.length mustBe 2
      }

    }

    "portfolio" must {

      "be able to create a Share Asset" in {
        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Shares).success.value
          .set(SharesInAPortfolioPage, true).success.value
          .set(SharePortfolioNamePage, "Portfolio").success.value
          .set(SharePortfolioQuantityInTrustPage, quantity).success.value
          .set(SharePortfolioValueInTrustPage, assetValue).success.value
          .set(SharePortfolioOnStockExchangePage, false).success.value
          .set(AssetStatus, Completed).success.value

        shareAssetMapper.build(userAnswers).value mustBe
            List(
              SharesType(
                numberOfShares = quantity.toString,
                orgName = "Portfolio",
                shareClass = "Other",
                typeOfShare = "Unquoted",
                value = assetValue
              )
            )
      }

      "be able to create multiple Share Assets" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Shares).success.value
          .set(SharesInAPortfolioPage, true).success.value
          .set(SharePortfolioNamePage, "Portfolio").success.value
          .set(SharePortfolioQuantityInTrustPage, quantity).success.value
          .set(SharePortfolioValueInTrustPage, assetValue).success.value
          .set(SharePortfolioOnStockExchangePage, false).success.value
          .set(AssetStatus, Completed).success.value
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Shares).success.value
          .set(SharesInAPortfolioPage, true).success.value
          .set(SharePortfolioNamePage, "Portfolio").success.value
          .set(SharePortfolioQuantityInTrustPage, quantity).success.value
          .set(SharePortfolioValueInTrustPage, assetValue).success.value
          .set(SharePortfolioOnStockExchangePage, false).success.value
          .set(AssetStatus, Completed).success.value

        shareAssetMapper.build(userAnswers).value.length mustBe 2
      }
    }
  }
}
