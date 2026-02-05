/*
 * Copyright 2025 HM Revenue & Customs
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

package pages.asset.shares

import models.{ShareClass, Status, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.AssetStatus
import pages.behaviours.PageBehaviours

class SharesInAPortfolioPageSpec extends PageBehaviours {

  private val assetValue: Long     = 2000L
  val page: SharesInAPortfolioPage = SharesInAPortfolioPage(index)

  "SharesInAPortfolioPage" must {

    beRetrievable[Boolean](page)

    beSettable[Boolean](page)

    beRemovable[Boolean](page)
  }

  "remove relevant data when ShareInAPortfolio is set to false" in
    forAll(arbitrary[UserAnswers]) { initial =>
      val answers: UserAnswers = initial
        .set(SharesInAPortfolioPage(index), false)
        .success
        .value
        .set(ShareCompanyNamePage(index), "Company")
        .success
        .value
        .set(SharesOnStockExchangePage(index), false)
        .success
        .value
        .set(ShareClassPage(index), ShareClass.Ordinary)
        .success
        .value
        .set(ShareQuantityInTrustPage(index), 20L)
        .success
        .value
        .set(ShareValueInTrustPage(index), assetValue)
        .success
        .value
        .set(AssetStatus(index), Status.Completed)
        .success
        .value

      val result = answers.set(SharesInAPortfolioPage(index), true).success.value

      result.get(SharesOnStockExchangePage(index)) mustNot be(defined)
      result.get(ShareCompanyNamePage(index)) mustNot be(defined)
      result.get(ShareClassPage(index)) mustNot be(defined)
      result.get(ShareQuantityInTrustPage(index)) mustNot be(defined)
      result.get(ShareValueInTrustPage(index)) mustNot be(defined)
      result.get(AssetStatus(index)) mustNot be(defined)
    }

  "remove relevant data when ShareInAPortfolio is set to true" in
    forAll(arbitrary[UserAnswers]) { initial =>
      val answers: UserAnswers = initial
        .set(SharesInAPortfolioPage(index), true)
        .success
        .value
        .set(SharePortfolioNamePage(index), "Shares")
        .success
        .value
        .set(SharePortfolioOnStockExchangePage(index), true)
        .success
        .value
        .set(SharePortfolioQuantityInTrustPage(index), 20L)
        .success
        .value
        .set(SharePortfolioValueInTrustPage(index), assetValue)
        .success
        .value
        .set(AssetStatus(index), Status.Completed)
        .success
        .value

      val result = answers.set(SharesInAPortfolioPage(index), false).success.value

      result.get(SharePortfolioNamePage(index)) mustNot be(defined)
      result.get(SharePortfolioOnStockExchangePage(index)) mustNot be(defined)
      result.get(SharePortfolioQuantityInTrustPage(index)) mustNot be(defined)
      result.get(SharePortfolioValueInTrustPage(index)) mustNot be(defined)
      result.get(AssetStatus(index)) mustNot be(defined)
    }

}
