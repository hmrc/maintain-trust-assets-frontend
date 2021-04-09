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
import controllers.asset.noneeabusiness.{routes => rts}
import controllers.asset.noneeabusiness.add.{routes => addRts}
import controllers.asset.noneeabusiness.amend.{routes => amendRts}
import generators.Generators
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.noneeabusiness._

class NonEeaBusinessNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[NonEeaBusinessNavigator]

  "Non-EEA Business Navigator" when {

    "adding" must {

      val mode = NormalMode

      "navigate from NamePage to InternationalAddressPage" in {

        val page = NamePage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(page, mode, userAnswers)
              .mustBe(rts.InternationalAddressController.onPageLoad(mode))
        }
      }

      "navigate from InternationalAddressPage to GoverningCountryPage" in {

        val page = NonUkAddressPage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(page, mode, userAnswers)
              .mustBe(rts.GoverningCountryController.onPageLoad(mode))
        }
      }

      "navigate from GoverningCountryPage to StartDatePage" in {

        val page = GoverningCountryPage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(page, mode, userAnswers)
              .mustBe(rts.StartDateController.onPageLoad(mode))
        }
      }

      "navigate from StartDatePage to Check Answers" in {

        val page = StartDatePage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(page, mode, userAnswers)
              .mustBe(addRts.AnswersController.onPageLoad())
        }
      }
    }

    "amending" must {

      val mode = CheckMode
      val index = 0

      "navigate from NamePage to InternationalAddressPage" in {

        val page = NamePage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(page, mode, userAnswers.set(IndexPage, index).success.value)
              .mustBe(rts.InternationalAddressController.onPageLoad(mode))
        }
      }

      "navigate from InternationalAddressPage to GoverningCountryPage" in {

        val page = NonUkAddressPage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(page, mode, userAnswers.set(IndexPage, index).success.value)
              .mustBe(rts.GoverningCountryController.onPageLoad(mode))
        }
      }

      "navigate from GoverningCountryPage to Check Answers" in {

        val page = GoverningCountryPage

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            navigator.nextPage(page, mode, userAnswers.set(IndexPage, index).success.value)
              .mustBe(amendRts.AnswersController.renderFromUserAnswers(index))
        }
      }
    }
  }
}
