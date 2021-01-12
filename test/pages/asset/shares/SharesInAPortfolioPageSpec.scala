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

package pages.asset.shares

import models.{ShareClass, Status, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.AssetStatus
import pages.behaviours.PageBehaviours

class SharesInAPortfolioPageSpec extends PageBehaviours {

  private val assetValue: Long = 2000L

  "SharesInAPortfolioPage" must {

    beRetrievable[Boolean](SharesInAPortfolioPage(0))

    beSettable[Boolean](SharesInAPortfolioPage(0))

    beRemovable[Boolean](SharesInAPortfolioPage(0))
  }

  "remove relevant data when ShareInAPortfolio is set to false" in {
    forAll(arbitrary[UserAnswers]) {
      initial =>
        val answers: UserAnswers = initial.set(SharesInAPortfolioPage(0), false).success.value
          .set(ShareCompanyNamePage(0), "Company").success.value
          .set(SharesOnStockExchangePage(0), false).success.value
          .set(ShareClassPage(0), ShareClass.Ordinary).success.value
          .set(ShareQuantityInTrustPage(0), 20L).success.value
          .set(ShareValueInTrustPage(0), assetValue).success.value
          .set(AssetStatus(0), Status.Completed).success.value

        val result = answers.set(SharesInAPortfolioPage(0), true).success.value

        result.get(SharesOnStockExchangePage(0)) mustNot be(defined)
        result.get(ShareCompanyNamePage(0)) mustNot be(defined)
        result.get(ShareClassPage(0)) mustNot be(defined)
        result.get(ShareQuantityInTrustPage(0)) mustNot be(defined)
        result.get(ShareValueInTrustPage(0)) mustNot be(defined)
        result.get(AssetStatus(0)) mustNot be(defined)
    }
  }

  "remove relevant data when ShareInAPortfolio is set to true" in {
    forAll(arbitrary[UserAnswers]) {
      initial =>
        val answers: UserAnswers = initial.set(SharesInAPortfolioPage(0), true).success.value
          .set(SharePortfolioNamePage(0), "Shares").success.value
          .set(SharePortfolioOnStockExchangePage(0), true).success.value
          .set(SharePortfolioQuantityInTrustPage(0), 20L).success.value
          .set(SharePortfolioValueInTrustPage(0), assetValue).success.value
          .set(AssetStatus(0), Status.Completed).success.value

        val result = answers.set(SharesInAPortfolioPage(0), false).success.value

        result.get(SharePortfolioNamePage(0)) mustNot be(defined)
        result.get(SharePortfolioOnStockExchangePage(0)) mustNot be(defined)
        result.get(SharePortfolioQuantityInTrustPage(0)) mustNot be(defined)
        result.get(SharePortfolioValueInTrustPage(0)) mustNot be(defined)
        result.get(AssetStatus(0)) mustNot be(defined)
    }
  }
}
