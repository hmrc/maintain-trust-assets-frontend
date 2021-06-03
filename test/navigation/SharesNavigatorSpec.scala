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

package navigation

import base.SpecBase
import controllers.asset.shares.routes._
import generators.Generators
import models.{NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.shares._
import pages.asset.shares.add.ShareAnswerPage

class SharesNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[SharesNavigator]

  "Shares Navigator" must {

    "go to SharePortfolioName from SharesInAPortfolio when user answers yes" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(SharesInAPortfolioPage, true).success.value

          navigator.nextPage(SharesInAPortfolioPage, NormalMode, answers)
            .mustBe(SharePortfolioNameController.onPageLoad(NormalMode))
      }
    }

    "go to SharePortfolioOnStockExchange from SharePortfolioName" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(SharePortfolioNamePage, NormalMode, userAnswers)
            .mustBe(SharePortfolioOnStockExchangeController.onPageLoad(NormalMode))
      }
    }

    "go to SharePortfolioQuantityInTrust from SharePortfolioOnStockExchange" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(SharePortfolioOnStockExchangePage, NormalMode, userAnswers)
            .mustBe(SharePortfolioQuantityInTrustController.onPageLoad(NormalMode))
      }
    }

    "go to SharePortfolioValueInTrust from SharePortfolioQuantityInTrust" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(SharePortfolioQuantityInTrustPage, NormalMode, userAnswers)
            .mustBe(SharePortfolioValueInTrustController.onPageLoad(NormalMode))
      }
    }

    "go to ShareAnswers from SharePortfolioValueInTrust" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(SharePortfolioValueInTrustPage, NormalMode, userAnswers)
            .mustBe(controllers.asset.shares.add.routes.ShareAnswerController.onPageLoad())
      }
    }

    "go to ShareCompanyName from SharesInAPortfolio when user answers no" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(SharesInAPortfolioPage, false).success.value

          navigator.nextPage(SharesInAPortfolioPage, NormalMode, answers)
            .mustBe(ShareCompanyNameController.onPageLoad(NormalMode))
      }
    }

    "go to SharesOnStockExchange from ShareCompanyName" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(ShareCompanyNamePage, NormalMode, userAnswers)
            .mustBe(SharesOnStockExchangeController.onPageLoad(NormalMode))
      }
    }

    "go to ShareClass from SharesOnStockExchange" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(SharesOnStockExchangePage, NormalMode, userAnswers)
            .mustBe(ShareClassController.onPageLoad(NormalMode))
      }
    }

    "go to ShareQuantityInTrust from ShareClass" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(ShareClassPage, NormalMode, userAnswers)
            .mustBe(ShareQuantityInTrustController.onPageLoad(NormalMode))
      }
    }

    "go to ShareValueInTrust from ShareQuantityInTrust" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(ShareQuantityInTrustPage, NormalMode, userAnswers)
            .mustBe(ShareValueInTrustController.onPageLoad(NormalMode))
      }
    }

    "go to ShareAnswers from ShareValueInTrust" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(ShareValueInTrustPage, NormalMode, userAnswers)
            .mustBe(controllers.asset.shares.add.routes.ShareAnswerController.onPageLoad())
      }
    }

    "go to AddAssetPage from ShareAnswerPage" in {
      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          navigator.nextPage(ShareAnswerPage, NormalMode, userAnswers)
            .mustBe(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
      }
    }
  }

}
