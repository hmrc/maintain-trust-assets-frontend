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
import controllers.asset.business.routes._
import generators.Generators
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.business._
import pages.asset.business.add.BusinessAnswerPage
import pages.asset.business.amend.IndexPage

class BusinessNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[BusinessNavigator]

  "Business Navigator" must {

    "navigate from BusinessNamePage to BusinessDescriptionPage" in {

      val page = BusinessNamePage(index)

      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(page, NormalMode, userAnswers)
          .mustBe(BusinessDescriptionController.onPageLoad(index, NormalMode))
      }
    }

    "navigate from BusinessDescriptionPage to BusinessAddressUkYesNoPage" in {

      val page = BusinessDescriptionPage(index)

      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(page, NormalMode, userAnswers)
          .mustBe(BusinessAddressUkYesNoController.onPageLoad(index, NormalMode))
      }
    }

    "navigate from BusinessAddressUkYesNoPage" when {

      val page = BusinessAddressUkYesNoPage(index)

      "yes selected" in
        forAll(arbitrary[UserAnswers]) { userAnswers =>
          val answers = userAnswers.set(page, true).success.value
          navigator
            .nextPage(page, NormalMode, answers)
            .mustBe(BusinessUkAddressController.onPageLoad(index, NormalMode))
        }

      "no selected" in
        forAll(arbitrary[UserAnswers]) { userAnswers =>
          val answers = userAnswers.set(page, false).success.value
          navigator
            .nextPage(page, NormalMode, answers)
            .mustBe(BusinessInternationalAddressController.onPageLoad(index, NormalMode))
        }
    }

    "navigate from BusinessUkAddressPage to BusinessValuePage" in {

      val page = BusinessUkAddressPage(index)

      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(page, NormalMode, userAnswers)
          .mustBe(BusinessValueController.onPageLoad(index, NormalMode))
      }
    }

    "navigate from BusinessInternationalAddressPage to BusinessValuePage" in {

      val page = BusinessInternationalAddressPage(index)

      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(page, NormalMode, userAnswers)
          .mustBe(BusinessValueController.onPageLoad(index, NormalMode))
      }
    }

    "navigate to amend Answers Page" when {
      "navigating from BusinessValuePage" in {

        val page = BusinessValuePage(index)

        forAll(arbitrary[UserAnswers]) { userAnswers =>
          val answers = userAnswers.set(page, 100L).success.value.set(IndexPage, index).success.value

          navigator
            .nextPage(page, CheckMode, answers)
            .mustBe(controllers.asset.business.amend.routes.BusinessAmendAnswersController.renderFromUserAnswers(index))
        }
      }
    }

    "navigate from BusinessAnswerPage" in {

      val page = BusinessAnswerPage(index)

      forAll(arbitrary[UserAnswers]) { userAnswers =>
        navigator
          .nextPage(page, NormalMode, userAnswers)
          .mustBe(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
      }
    }
  }

}
