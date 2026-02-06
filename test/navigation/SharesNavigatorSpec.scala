/*
 * Copyright 2026 HM Revenue & Customs
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
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.shares._
import pages.asset.shares.add.ShareAnswerPage
import pages.asset.shares.amend.IndexPage

class SharesNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[SharesNavigator]

  "Shares Navigator" must {

    "go to SharePortfolioName from SharesInAPortfolio when user answers yes" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        val answers = userAnswers.set(SharesInAPortfolioPage(index), true).success.value

        navigator
          .nextPage(SharesInAPortfolioPage(index), NormalMode, answers)
          .mustBe(SharePortfolioNameController.onPageLoad(index, NormalMode))
      }

    "go to SharePortfolioOnStockExchange from SharePortfolioName" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(SharePortfolioNamePage(index), NormalMode, userAnswers)
          .mustBe(SharePortfolioOnStockExchangeController.onPageLoad(index, NormalMode))
      }

    "go to SharePortfolioQuantityInTrust from SharePortfolioOnStockExchange" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(SharePortfolioOnStockExchangePage(index), NormalMode, userAnswers)
          .mustBe(SharePortfolioQuantityInTrustController.onPageLoad(index, NormalMode))
      }

    "go to SharePortfolioValueInTrust from SharePortfolioQuantityInTrust" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(SharePortfolioQuantityInTrustPage(index), NormalMode, userAnswers)
          .mustBe(SharePortfolioValueInTrustController.onPageLoad(index, NormalMode))
      }

    "go to ShareAnswers from SharePortfolioValueInTrust" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(SharePortfolioValueInTrustPage(index), NormalMode, userAnswers)
          .mustBe(controllers.asset.shares.add.routes.ShareAnswerController.onPageLoad(index))
      }

    "go to ShareCompanyName from SharesInAPortfolio when user answers no" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        val answers = userAnswers.set(SharesInAPortfolioPage(index), false).success.value

        navigator
          .nextPage(SharesInAPortfolioPage(index), NormalMode, answers)
          .mustBe(ShareCompanyNameController.onPageLoad(index, NormalMode))
      }

    "go to SharesOnStockExchange from ShareCompanyName" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(ShareCompanyNamePage(index), NormalMode, userAnswers)
          .mustBe(SharesOnStockExchangeController.onPageLoad(index, NormalMode))
      }

    "go to ShareClass from SharesOnStockExchange" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(SharesOnStockExchangePage(index), NormalMode, userAnswers)
          .mustBe(ShareClassController.onPageLoad(index, NormalMode))
      }

    "go to ShareQuantityInTrust from ShareClass" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(ShareClassPage(index), NormalMode, userAnswers)
          .mustBe(ShareQuantityInTrustController.onPageLoad(index, NormalMode))
      }

    "go to ShareValueInTrust from ShareQuantityInTrust" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(ShareQuantityInTrustPage(index), NormalMode, userAnswers)
          .mustBe(ShareValueInTrustController.onPageLoad(index, NormalMode))
      }

    "navigate to Answers Page for add" when {
      "go to ShareAnswers from ShareValueInTrust" in
        forAll(arbitrary[UserAnswers]) { userAnswers =>
          navigator
            .nextPage(ShareValueInTrustPage(index), NormalMode, userAnswers)
            .mustBe(controllers.asset.shares.add.routes.ShareAnswerController.onPageLoad(index))
        }
    }

    "go to AddAssetPage from ShareAnswerPage" in
      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(ShareAnswerPage(index), NormalMode, userAnswers)
          .mustBe(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
      }

    "navigate to amend Answers Page" when {
      "go to ShareAnswers from ShareValueInTrust" in {
        val page = ShareValueInTrustPage(index)

        forAll(arbitrary[UserAnswers]) { userAnswers =>
          val answers = userAnswers.set(page, 100L).success.value.set(IndexPage, index).success.value

          navigator
            .nextPage(page, CheckMode, answers)
            .mustBe(controllers.asset.shares.amend.routes.ShareAmendAnswersController.renderFromUserAnswers(index))
        }
      }
    }
  }

}
