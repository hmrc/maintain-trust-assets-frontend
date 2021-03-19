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
import controllers.asset.business.routes._
import generators.Generators
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.business._

class BusinessNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[BusinessNavigator]
  private val index: Int = 0

  "Business Navigator" must {

    "navigate from BusinessNamePage to BusinessDescriptionPage" in {

      val page = BusinessNamePage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, fakeDraftId)(userAnswers)
            .mustBe(BusinessDescriptionController.onPageLoad(index, fakeDraftId))
      }
    }

    "navigate from BusinessDescriptionPage to BusinessAddressUkYesNoPage" in {

      val page = BusinessDescriptionPage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, fakeDraftId)(userAnswers)
            .mustBe(BusinessAddressUkYesNoController.onPageLoad(index, fakeDraftId))
      }
    }

    "navigate from BusinessAddressUkYesNoPage" when {

      val page = BusinessAddressUkYesNoPage(index)

      "yes selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, true).success.value
            navigator.nextPage(page, fakeDraftId)(answers)
              .mustBe(BusinessUkAddressController.onPageLoad(index, fakeDraftId))
        }
      }

      "no selected" in {

        forAll(arbitrary[UserAnswers]) {
          userAnswers =>
            val answers = userAnswers.set(page, false).success.value
            navigator.nextPage(page, fakeDraftId)(answers)
              .mustBe(BusinessInternationalAddressController.onPageLoad(index, fakeDraftId))
        }
      }
    }

    "navigate from BusinessUkAddressPage to BusinessValuePage" in {

      val page = BusinessUkAddressPage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, fakeDraftId)(userAnswers)
            .mustBe(BusinessValueController.onPageLoad(index, fakeDraftId))
      }
    }

    "navigate from BusinessInternationalAddressPage to BusinessValuePage" in {

      val page = BusinessInternationalAddressPage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, fakeDraftId)(userAnswers)
            .mustBe(BusinessValueController.onPageLoad(index, fakeDraftId))
      }
    }
  }
}
