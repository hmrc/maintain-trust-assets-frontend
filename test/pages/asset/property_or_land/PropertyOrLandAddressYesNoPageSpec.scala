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

package pages.asset.property_or_land

import models.{NonUkAddress, UkAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class PropertyOrLandAddressYesNoPageSpec extends PageBehaviours {

  val page = PropertyOrLandAddressYesNoPage

  "PropertyOrLandAddressYesNoPage" must {

    beRetrievable[Boolean](page)

    beSettable[Boolean](page)

    beRemovable[Boolean](page)

    "remove relevant data" when {

      "set to false" in {
        forAll(arbitrary[UserAnswers]) {
          initial =>
            val answers: UserAnswers = initial.set(page, true).success.value
              .set(PropertyOrLandAddressUkYesNoPage, true).success.value
              .set(PropertyOrLandInternationalAddressPage,  NonUkAddress("line 1", "line 2", None, "France")).success.value
              .set(PropertyOrLandUKAddressPage,  UkAddress("line 1", "line2", None, None, "NE1 1NE")).success.value

            val result = answers.set(page, false).success.value

            result.get(PropertyOrLandAddressUkYesNoPage) must not be defined
            result.get(PropertyOrLandInternationalAddressPage) must not be defined
            result.get(PropertyOrLandUKAddressPage) must not be defined
        }
      }

      "set to true" in {
        forAll(arbitrary[UserAnswers]) {
          initial =>
            val answers: UserAnswers = initial.set(page, false).success.value
              .set(PropertyOrLandDescriptionPage, "Test").success.value

            val result = answers.set(page, true).success.value

            result.get(PropertyOrLandDescriptionPage) must not be defined
        }
      }
    }

  }
}
