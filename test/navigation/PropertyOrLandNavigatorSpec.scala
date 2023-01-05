/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.asset.property_or_land.routes._
import generators.Generators
import models.{CheckMode, NonUkAddress, NormalMode, UkAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.property_or_land._
import pages.asset.property_or_land.add.PropertyOrLandAnswerPage
import pages.asset.property_or_land.amend.IndexPage

class PropertyOrLandNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[PropertyOrLandNavigator]

  "Property or Land Navigator" must {

    "navigate from PropertyOrLandAddressYesNoPage" when {

      val page = PropertyOrLandAddressYesNoPage

      "user answers yes to go to PropertyOrLandAddressUkYesNoPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, value = true).success.value
            navigator.nextPage(page, NormalMode, answers)
              .mustBe(PropertyOrLandAddressUkYesNoController.onPageLoad(NormalMode))
        }
      }

      "user answers no to go to PropertyOrLandDescriptionPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, value = false).success.value
            navigator.nextPage(page, NormalMode, answers)
              .mustBe(PropertyOrLandDescriptionController.onPageLoad(NormalMode))
        }
      }

    }

    "navigate from PropertyOrLandAddressUkYesNoPage" when {

      val page = PropertyOrLandAddressUkYesNoPage

      "user answers yes to go to PropertyOrLandUKAddressPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, value = true).success.value
            navigator.nextPage(page, NormalMode, answers)
              .mustBe(PropertyOrLandUKAddressController.onPageLoad(NormalMode))
        }
      }

      "user answers no to go to PropertyOrLandInternationalAddressPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, value = false).success.value
            navigator.nextPage(page, NormalMode, answers)
              .mustBe(PropertyOrLandInternationalAddressController.onPageLoad(NormalMode))
        }
      }
    }

    "navigate to PropertyOrLandTotalValuePage" when {
      "navigating from PropertyOrLandDescriptionPage" in {

        val page = PropertyOrLandDescriptionPage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, "Test").success.value
            navigator.nextPage(page, NormalMode, answers)
              .mustBe(PropertyOrLandTotalValueController.onPageLoad(NormalMode))
        }
      }
      "navigating from PropertyOrLandInternationalAddressPage" in {

        val page = PropertyOrLandInternationalAddressPage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, NonUkAddress("line1", "line2", None, "France")).success.value
            navigator.nextPage(page, NormalMode, answers)
              .mustBe(PropertyOrLandTotalValueController.onPageLoad(NormalMode))
        }
      }
      "navigating from PropertyOrLandUKAddressPage" in {

        val page = PropertyOrLandUKAddressPage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, UkAddress("line1", "line2", None, None, "NE11NE")).success.value
            navigator.nextPage(page, NormalMode, answers)
              .mustBe(PropertyOrLandTotalValueController.onPageLoad(NormalMode))
        }
      }
    }

    "navigate from PropertyOrLandTotalValuePage to TrustOwnAllThePropertyOrLandPage" in {

      val page = PropertyOrLandTotalValuePage

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val answers = userAnswers.set(page, 100L).success.value
          navigator.nextPage(page, NormalMode, answers)
            .mustBe(TrustOwnAllThePropertyOrLandController.onPageLoad(NormalMode))
      }
    }

    "navigate from TrustOwnAllThePropertyOrLandPage" when {

      val page = TrustOwnAllThePropertyOrLandPage

      "user answers yes to go to PropertyLandValueTrustPage" in {
        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, value = false).success.value
            navigator.nextPage(page, NormalMode, answers)
              .mustBe(PropertyLandValueTrustController.onPageLoad(NormalMode))
        }
      }

    }

    "navigate to PropertyOrLandAnswerPage" when {
      "navigating from PropertyLandValueTrustPage" in {

        val page = PropertyLandValueTrustPage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, 100L).success.value
            navigator.nextPage(page, NormalMode, answers)
              .mustBe(controllers.asset.property_or_land.add.routes.PropertyOrLandAnswerController.onPageLoad())
        }
      }

      "navigating from TrustOwnAllThePropertyOrLandPage when user answers no" in {

        val page = TrustOwnAllThePropertyOrLandPage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, true).success.value
            navigator.nextPage(page, NormalMode, answers)
              .mustBe(controllers.asset.property_or_land.add.routes.PropertyOrLandAnswerController.onPageLoad())
        }
      }
    }

    "navigate to amend Answers Page" when {
      "navigating from PropertyLandValueTrustPage" in {

        val page = PropertyLandValueTrustPage
        val index = 0

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, 100L).success.value.set(IndexPage, index).success.value

            navigator.nextPage(page, CheckMode, answers)
              .mustBe(controllers.asset.property_or_land.amend.routes.PropertyOrLandAmendAnswersController.renderFromUserAnswers(index))
        }
      }

      "navigating from TrustOwnAllThePropertyOrLandPage when user answers no" in {

        val page = TrustOwnAllThePropertyOrLandPage
        val index = 0

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, true).success.value.set(IndexPage, index).success.value

            navigator.nextPage(page, CheckMode, answers)
              .mustBe(controllers.asset.property_or_land.amend.routes.PropertyOrLandAmendAnswersController.renderFromUserAnswers(index))
        }
      }

      "navigating from PropertyLandTrustOwnAllThePropertyOrLandPage fails if we have no index" in {

        val page = TrustOwnAllThePropertyOrLandPage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, true).success.value
            navigator.nextPage(page, CheckMode, answers).mustBe(controllers.routes.SessionExpiredController.onPageLoad)
        }
      }
    }

    "navigate from PropertyOrLandAnswerPage" in {

      val page = PropertyOrLandAnswerPage

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, NormalMode, userAnswers)
            .mustBe(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
      }
    }
  }

}
