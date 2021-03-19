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

import models.{InternationalAddress, UKAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class PropertyOrLandAddressYesNoPageSpec extends PageBehaviours {

  "PropertyOrLandAddressYesNoPage" must {

    beRetrievable[Boolean](PropertyOrLandAddressYesNoPage(0))

    beSettable[Boolean](PropertyOrLandAddressYesNoPage(0))

    beRemovable[Boolean](PropertyOrLandAddressYesNoPage(0))

    "remove relevant data" when {

      val page = PropertyOrLandAddressYesNoPage(0)

      "set to false" in {
        forAll(arbitrary[UserAnswers]) {
          initial =>
            val answers: UserAnswers = initial.set(page, true).success.value
              .set(PropertyOrLandAddressUkYesNoPage(0), true).success.value
              .set(PropertyOrLandInternationalAddressPage(0),  InternationalAddress("line 1", "line 2", None, "France")).success.value
              .set(PropertyOrLandUKAddressPage(0),  UKAddress("line 1", "line2", None, None, "NE1 1NE")).success.value

            val result = answers.set(page, false).success.value

            result.get(PropertyOrLandAddressUkYesNoPage(0)) must not be defined
            result.get(PropertyOrLandInternationalAddressPage(0)) must not be defined
            result.get(PropertyOrLandUKAddressPage(0)) must not be defined
        }
      }

      "set to true" in {
        forAll(arbitrary[UserAnswers]) {
          initial =>
            val answers: UserAnswers = initial.set(page, false).success.value
              .set(PropertyOrLandDescriptionPage(0), "Test").success.value

            val result = answers.set(page, true).success.value

            result.get(PropertyOrLandDescriptionPage(0)) must not be defined
        }
      }
    }

  }
}
