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

package navigation

import base.SpecBase
import controllers.asset.routes
import controllers.asset.shares.routes._
import generators.Generators
import models.{NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.shares._

class SharesNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[SharesNavigator]
  private val index: Int = 0

  "Shares Navigator" must {

    "go to SharePortfolioName from SharesInAPortfolio when user answers yes" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(SharesInAPortfolioPage(index), true).success.value

          navigator.nextPage(SharesInAPortfolioPage(index), NormalMode, fakeDraftId)(answers)
            .mustBe(SharePortfolioNameController.onPageLoad(NormalMode, index, fakeDraftId))
      }
    }

    "go to SharePortfolioOnStockExchange from SharePortfolioName" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(SharePortfolioNamePage(index), NormalMode, fakeDraftId)(userAnswers)
            .mustBe(SharePortfolioOnStockExchangeController.onPageLoad(NormalMode, index, fakeDraftId))
      }
    }

    "go to SharePortfolioQuantityInTrust from SharePortfolioOnStockExchange" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(SharePortfolioOnStockExchangePage(index), NormalMode, fakeDraftId)(userAnswers)
            .mustBe(SharePortfolioQuantityInTrustController.onPageLoad(NormalMode, index, fakeDraftId))
      }
    }

    "go to SharePortfolioValueInTrust from SharePortfolioQuantityInTrust" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(SharePortfolioQuantityInTrustPage(index), NormalMode, fakeDraftId)(userAnswers)
            .mustBe(SharePortfolioValueInTrustController.onPageLoad(NormalMode, index, fakeDraftId))
      }
    }

    "go to ShareAnswers from SharePortfolioValueInTrust" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(SharePortfolioValueInTrustPage(index), NormalMode, fakeDraftId)(userAnswers)
            .mustBe(ShareAnswerController.onPageLoad(index, fakeDraftId))
      }
    }

    "go to ShareCompanyName from SharesInAPortfolio when user answers no" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(SharesInAPortfolioPage(index), false).success.value

          navigator.nextPage(SharesInAPortfolioPage(index), NormalMode, fakeDraftId)(answers)
            .mustBe(ShareCompanyNameController.onPageLoad(NormalMode, index, fakeDraftId))
      }
    }

    "go to SharesOnStockExchange from ShareCompanyName" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(ShareCompanyNamePage(index), NormalMode, fakeDraftId)(userAnswers)
            .mustBe(SharesOnStockExchangeController.onPageLoad(NormalMode, index, fakeDraftId))
      }
    }

    "go to ShareClass from SharesOnStockExchange" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(SharesOnStockExchangePage(index), NormalMode, fakeDraftId)(userAnswers)
            .mustBe(ShareClassController.onPageLoad(NormalMode, index, fakeDraftId))
      }
    }

    "go to ShareQuantityInTrust from ShareClass" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(ShareClassPage(index), NormalMode, fakeDraftId)(userAnswers)
            .mustBe(ShareQuantityInTrustController.onPageLoad(NormalMode, index, fakeDraftId))
      }
    }

    "go to ShareValueInTrust from ShareQuantityInTrust" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(ShareQuantityInTrustPage(index), NormalMode, fakeDraftId)(userAnswers)
            .mustBe(ShareValueInTrustController.onPageLoad(NormalMode, index, fakeDraftId))
      }
    }

    "go to ShareAnswers from ShareValueInTrust" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(ShareValueInTrustPage(index), NormalMode, fakeDraftId)(userAnswers)
            .mustBe(ShareAnswerController.onPageLoad(index, fakeDraftId))
      }
    }

    "go to AddAssetPage from ShareAnswerPage" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(ShareAnswerPage, NormalMode, fakeDraftId)(userAnswers)
            .mustBe(routes.AddAssetsController.onPageLoad(fakeDraftId))
      }
    }
  }

}
