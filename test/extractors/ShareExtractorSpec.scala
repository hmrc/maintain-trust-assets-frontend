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

package extractors

import base.SpecBase
import models.assets.SharesType
import models.{ShareClass, UserAnswers}
import pages.asset.shares._
import pages.asset.shares.amend.IndexPage
import utils.Constants.{QUOTED, UNQUOTED}

class ShareExtractorSpec extends SpecBase {

  private val index = 0
  private val name: String = "OrgName"
  private val assetValue: Long = 300L
  private val quantity: Long = 20

  private val extractor = new ShareExtractor()

  "ShareExtractor" must {

    "Populate user answers" when {

      val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = false)

      "portfolio with UnQuoted shares" in {
        val sharesAsset = SharesType(
          numberOfShares = quantity.toString,
          orgName = name,
          shareClass = "Other",
          typeOfShare = UNQUOTED,
          value = assetValue,
          isPortfolio = Some(true)
        )

        val result = extractor(baseAnswers, sharesAsset, index).get

        result.get(IndexPage) mustBe Some(index)
        result.get(SharesInAPortfolioPage) mustBe Some(true)
        result.get(SharePortfolioQuantityInTrustPage) mustBe Some(quantity)
        result.get(SharePortfolioNamePage) mustBe Some(name)
        result.get(ShareClassPage) mustBe Some(ShareClass.Other)
        result.get(SharePortfolioOnStockExchangePage) mustBe Some(false)
        result.get(SharePortfolioValueInTrustPage) mustBe Some(assetValue)
      }

      "portfolio with Quoted shares" in {
        val sharesAsset = SharesType(
          numberOfShares = quantity.toString,
          orgName = name,
          shareClass = "Other",
          typeOfShare = QUOTED,
          value = assetValue,
          isPortfolio = Some(true)
        )

        val result = extractor(baseAnswers, sharesAsset, index).get

        result.get(IndexPage) mustBe Some(index)
        result.get(SharesInAPortfolioPage) mustBe Some(true)
        result.get(SharePortfolioQuantityInTrustPage) mustBe Some(quantity)
        result.get(SharePortfolioNamePage) mustBe Some(name)
        result.get(ShareClassPage) mustBe Some(ShareClass.Other)
        result.get(SharePortfolioOnStockExchangePage) mustBe Some(true)
        result.get(SharePortfolioValueInTrustPage) mustBe Some(assetValue)
      }

      "non-portfolio with UnQuoted shares" in {
        val sharesAsset = SharesType(
          numberOfShares = quantity.toString,
          orgName = name,
          shareClass = "Deferred ordinary shares",
          typeOfShare = UNQUOTED,
          value = assetValue,
          isPortfolio = Some(false)
        )

        val result = extractor(baseAnswers, sharesAsset, index).get

        result.get(IndexPage) mustBe Some(index)
        result.get(SharesInAPortfolioPage) mustBe Some(false)
        result.get(ShareQuantityInTrustPage) mustBe Some(quantity)
        result.get(ShareCompanyNamePage) mustBe Some(name)
        result.get(ShareClassPage) mustBe Some(ShareClass.Deferred)
        result.get(SharesOnStockExchangePage) mustBe Some(false)
        result.get(ShareValueInTrustPage) mustBe Some(assetValue)
      }

      "non-portfolio with Quoted shares" in {
        val sharesAsset = SharesType(
          numberOfShares = quantity.toString,
          orgName = name,
          shareClass = "Deferred ordinary shares",
          typeOfShare = QUOTED,
          value = assetValue,
          isPortfolio = None
        )

        val result = extractor(baseAnswers, sharesAsset, index).get

        result.get(IndexPage) mustBe Some(index)
        result.get(SharesInAPortfolioPage) mustBe Some(false)
        result.get(ShareQuantityInTrustPage) mustBe Some(quantity)
        result.get(ShareCompanyNamePage) mustBe Some(name)
        result.get(ShareClassPage) mustBe Some(ShareClass.Deferred)
        result.get(SharesOnStockExchangePage) mustBe Some(true)
        result.get(ShareValueInTrustPage) mustBe Some(assetValue)
      }
    }

  }

}
