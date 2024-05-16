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

package pages.asset.shares

import models.{ShareClass, Status, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.AssetStatus
import pages.behaviours.PageBehaviours

class SharesInAPortfolioPageSpec extends PageBehaviours {

  private val assetValue: Long = 2000L
  val page: SharesInAPortfolioPage.type = SharesInAPortfolioPage

  "SharesInAPortfolioPage" must {

    beRetrievable[Boolean](page)

    beSettable[Boolean](page)

    beRemovable[Boolean](page)
  }

  "remove relevant data when ShareInAPortfolio is set to false" in {
    forAll(arbitrary[UserAnswers]) {
      initial =>
        val answers: UserAnswers = initial.set(SharesInAPortfolioPage, false).success.value
          .set(ShareCompanyNamePage, "Company").success.value
          .set(SharesOnStockExchangePage, false).success.value
          .set(ShareClassPage, ShareClass.Ordinary).success.value
          .set(ShareQuantityInTrustPage, 20L).success.value
          .set(ShareValueInTrustPage, assetValue).success.value
          .set(AssetStatus, Status.Completed).success.value

        val result = answers.set(SharesInAPortfolioPage, true).success.value

        result.get(SharesOnStockExchangePage) mustNot be(defined)
        result.get(ShareCompanyNamePage) mustNot be(defined)
        result.get(ShareClassPage) mustNot be(defined)
        result.get(ShareQuantityInTrustPage) mustNot be(defined)
        result.get(ShareValueInTrustPage) mustNot be(defined)
        result.get(AssetStatus) mustNot be(defined)
    }
  }

  "remove relevant data when ShareInAPortfolio is set to true" in {
    forAll(arbitrary[UserAnswers]) {
      initial =>
        val answers: UserAnswers = initial.set(SharesInAPortfolioPage, true).success.value
          .set(SharePortfolioNamePage, "Shares").success.value
          .set(SharePortfolioOnStockExchangePage, true).success.value
          .set(SharePortfolioQuantityInTrustPage, 20L).success.value
          .set(SharePortfolioValueInTrustPage, assetValue).success.value
          .set(AssetStatus, Status.Completed).success.value

        val result = answers.set(SharesInAPortfolioPage, false).success.value

        result.get(SharePortfolioNamePage) mustNot be(defined)
        result.get(SharePortfolioOnStockExchangePage) mustNot be(defined)
        result.get(SharePortfolioQuantityInTrustPage) mustNot be(defined)
        result.get(SharePortfolioValueInTrustPage) mustNot be(defined)
        result.get(AssetStatus) mustNot be(defined)
    }
  }
}
