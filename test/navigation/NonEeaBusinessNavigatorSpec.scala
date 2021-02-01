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
import controllers.asset.noneeabusiness.routes._
import generators.Generators
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.noneeabusiness._

class NonEeaBusinessNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[NonEeaBusinessNavigator]
  private val index: Int = 0

  "Non-EEA Business Navigator" must {

    "navigate from NamePage to AddressUkYesNoPage" in {

      val page = NamePage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, fakeDraftId)(userAnswers)
            .mustBe(AddressUkYesNoController.onPageLoad(index, fakeDraftId))
      }
    }

    "navigate from AddressUkYesNoPage" when {

      val page = AddressUkYesNoPage(index)

      "yes selected to UkAddressPage" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, true).success.value
            navigator.nextPage(page, fakeDraftId)(answers)
              .mustBe(UkAddressController.onPageLoad(index, fakeDraftId))
        }
      }

      "no selected to InternationalAddressPage" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, false).success.value
            navigator.nextPage(page, fakeDraftId)(answers)
              .mustBe(InternationalAddressController.onPageLoad(index, fakeDraftId))
        }
      }
    }

    "navigate from UkAddressPage to GoverningCountryPage" in {

      val page = UkAddressPage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, fakeDraftId)(userAnswers)
            .mustBe(GoverningCountryController.onPageLoad(index, fakeDraftId))
      }
    }

    "navigate from InternationalAddressPage to GoverningCountryPage" in {

      val page = InternationalAddressPage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, fakeDraftId)(userAnswers)
            .mustBe(GoverningCountryController.onPageLoad(index, fakeDraftId))
      }
    }

    "navigate from GoverningCountryPage to StartDatePage" in {

      val page = GoverningCountryPage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, fakeDraftId)(userAnswers)
            .mustBe(StartDateController.onPageLoad(index, fakeDraftId))
      }
    }

    "navigate from StartDatePage to Check Answers" ignore {

      val page = StartDatePage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, fakeDraftId)(userAnswers)
            .mustBe(???)
      }
    }
  }
}
