/*
 * Copyright 2022 HM Revenue & Customs
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

package pages.asset.business

import models.{NonUkAddress, UkAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class BusinessAddressUkYesNoPageSpec extends PageBehaviours {

  private val page = BusinessAddressUkYesNoPage

  "BusinessAddressUkYesNoPage" must {

    beRetrievable[Boolean](BusinessAddressUkYesNoPage)

    beSettable[Boolean](BusinessAddressUkYesNoPage)

    beRemovable[Boolean](BusinessAddressUkYesNoPage)

    "remove relevant data" when {

      "set to true" in {
        forAll(arbitrary[UserAnswers]) {
          initial =>
            val answers: UserAnswers = initial.set(page, false).success.value
              .set(BusinessInternationalAddressPage, NonUkAddress("line 1", "line 2", None, "France")).success.value

            val result = answers.set(page, true).success.value

            result.get(BusinessInternationalAddressPage) must not be defined
        }
      }

      "set to false" in {
        forAll(arbitrary[UserAnswers]) {
          initial =>
            val answers: UserAnswers = initial.set(page, true).success.value
              .set(BusinessUkAddressPage, UkAddress("line 1", "line 2", None, None, "NE1 1NE")).success.value

            val result = answers.set(page, false).success.value

            result.get(BusinessUkAddressPage) must not be defined
        }
      }
    }

    "keep relevant data" when {

      "set to true and UK address already defined" in {
        forAll(arbitrary[UserAnswers]) {
          initial =>
            val answers: UserAnswers = initial
              .set(BusinessUkAddressPage, UkAddress("line 1", "line 2", None, None, "NE1 1NE")).success.value

            val result = answers.set(page, true).success.value

            result.get(BusinessUkAddressPage) must be(defined)
        }
      }

      "set to false and international address already defined" in {
        forAll(arbitrary[UserAnswers]) {
          initial =>
            val answers: UserAnswers = initial
              .set(BusinessInternationalAddressPage, NonUkAddress("line 1", "line 2", None, "France")).success.value

            val result = answers.set(page, false).success.value

            result.get(BusinessInternationalAddressPage) must be(defined)
        }
      }
    }
  }
}
