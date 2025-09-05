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

package mapping

import base.SpecBase
import generators.Generators
import models.ShareClass
import models.WhatKindOfAsset.Shares
import models.assets.SharesType
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import pages.asset._
import pages.asset.shares._

class ShareAssetMapperSpec extends SpecBase with Matchers
  with OptionValues with Generators {

  val shareAssetMapper: ShareAssetMapper = injector.instanceOf[ShareAssetMapper]

  private val assetValue: Long = 300L
  private val quantity: Long = 20L

  "ShareAssetMapper" must {
    "not be able to create a share asset when missing values in user answers" in {

      val userAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage(index), Shares).success.value
        .set(SharesInAPortfolioPage(index), true).success.value


      shareAssetMapper(userAnswers).isDefined mustBe false
    }

    "non-portfolio" must {

      "be able to create a Share Asset" in {
        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(index), Shares).success.value
          .set(SharesInAPortfolioPage(index), false).success.value
          .set(ShareCompanyNamePage(index), "Non-Portfolio").success.value
          .set(ShareQuantityInTrustPage(index), quantity).success.value
          .set(ShareValueInTrustPage(index), assetValue).success.value
          .set(SharesOnStockExchangePage(index), true).success.value
          .set(ShareClassPage(index), ShareClass.Deferred).success.value


        val result = shareAssetMapper(userAnswers)
        result.isDefined mustBe true

        result.get mustBe
          SharesType(
            numberOfShares = quantity.toString,
            orgName = "Non-Portfolio",
            shareClass = "Deferred ordinary shares",
            typeOfShare = "Quoted",
            value = assetValue,
            isPortfolio = Some(false),
            shareClassDisplay = Some(ShareClass.Deferred)
          )
      }
    }

    "portfolio" must {

      "be able to create a Share Asset" in {
        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(index), Shares).success.value
          .set(SharesInAPortfolioPage(index), true).success.value
          .set(SharePortfolioNamePage(index), "Portfolio").success.value
          .set(SharePortfolioQuantityInTrustPage(index), quantity).success.value
          .set(SharePortfolioValueInTrustPage(index), assetValue).success.value
          .set(SharePortfolioOnStockExchangePage(index), false).success.value


        val result = shareAssetMapper(userAnswers)
        result.isDefined mustBe true
        result.get mustBe
          SharesType(
            numberOfShares = quantity.toString,
            orgName = "Portfolio",
            shareClass = "Other",
            typeOfShare = "Unquoted",
            value = assetValue,
            isPortfolio = Some(true),
            shareClassDisplay = Some(ShareClass.Other)
          )
      }
    }
  }
}
