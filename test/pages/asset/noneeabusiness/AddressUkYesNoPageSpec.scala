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

package pages.asset.noneeabusiness

import models.{InternationalAddress, UKAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class AddressUkYesNoPageSpec extends PageBehaviours {

  private val index: Int = 0
  private val page: AddressUkYesNoPage = AddressUkYesNoPage(index)

  "AddressUkYesNoPage" must {

    beRetrievable[Boolean](page)

    beSettable[Boolean](page)

    beRemovable[Boolean](page)

    "implement cleanup logic" when {

      "yes selected" in {

        forAll(arbitrary[UserAnswers], arbitrary[InternationalAddress]) {
          (initial, address) =>
            val answers: UserAnswers = initial
              .set(page, false).success.value
              .set(InternationalAddressPage(index), address).success.value

            val result = answers.set(page, true).success.value

            result.get(InternationalAddressPage(index)) must not be defined
        }
      }

      "no selected" in {

        forAll(arbitrary[UserAnswers], arbitrary[UKAddress]) {
          (initial, address) =>
            val answers: UserAnswers = initial
              .set(page, true).success.value
              .set(UkAddressPage(index), address).success.value

            val result = answers.set(page, false).success.value

            result.get(UkAddressPage(index)) must not be defined
        }
      }
    }
  }
}
