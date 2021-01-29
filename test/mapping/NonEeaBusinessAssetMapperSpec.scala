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

package mapping

import base.SpecBase
import models.Status.Completed
import models.{AddressType, InternationalAddress, NonEeaBusinessType, UKAddress}
import models.WhatKindOfAsset.NonEeaBusiness
import pages.AssetStatus
import pages.asset._
import pages.asset.noneeabusiness._

import java.time.LocalDate

class NonEeaBusinessAssetMapperSpec extends SpecBase {

  private val mapper: NonEeaBusinessAssetMapper = injector.instanceOf[NonEeaBusinessAssetMapper]

  private val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true)

  private val name: String = "Name"
  private val ukAddress: UKAddress = UKAddress("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), "AB1 1AB")
  private val ukAddressType: AddressType = AddressType("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), Some("AB1 1AB"), "GB")
  private val nonUkAddress: InternationalAddress = InternationalAddress("Line 1", "Line 2", Some("Line 3"), "FR")
  private val nonUkAddressType: AddressType = AddressType("Line 1", "Line 2", Some("Line 3"), None, None, "FR")
  private val country: String = "GB"
  private val date: LocalDate = LocalDate.parse("1996-02-03")

  "NonEeaBusinessAssetMapper" must {

    "not be able to create a non-EEA business asset" when {

      "not all questions answered" in {

        val answers = baseAnswers

        mapper.build(answers) mustNot be(defined)
      }
    }

    "be able to create a non-EEA business asset" when {

      "UK address" in {

        val answers = baseAnswers
          .set(WhatKindOfAssetPage(0), NonEeaBusiness).success.value
          .set(NamePage(0), name).success.value
          .set(AddressUkYesNoPage(0), true).success.value
          .set(UkAddressPage(0), ukAddress).success.value
          .set(GoverningCountryPage(0), country).success.value
          .set(StartDatePage(0), date).success.value
          .set(AssetStatus(0), Completed).success.value

        val result = mapper.build(answers).get

        result mustBe List(
          NonEeaBusinessType(
            orgName = name,
            address = ukAddressType,
            govLawCountry = country,
            startDate = date
          )
        )
      }

      "non-UK address" in {

        val answers = baseAnswers
          .set(WhatKindOfAssetPage(0), NonEeaBusiness).success.value
          .set(NamePage(0), name).success.value
          .set(AddressUkYesNoPage(0), false).success.value
          .set(InternationalAddressPage(0), nonUkAddress).success.value
          .set(GoverningCountryPage(0), country).success.value
          .set(StartDatePage(0), date).success.value
          .set(AssetStatus(0), Completed).success.value

        val result = mapper.build(answers).get

        result mustBe List(
          NonEeaBusinessType(
            orgName = name,
            address = nonUkAddressType,
            govLawCountry = country,
            startDate = date
          )
        )
      }
      
      "multiple assets" in {

        val answers = baseAnswers
          .set(WhatKindOfAssetPage(0), NonEeaBusiness).success.value
          .set(NamePage(0), name).success.value
          .set(AddressUkYesNoPage(0), true).success.value
          .set(UkAddressPage(0), ukAddress).success.value
          .set(GoverningCountryPage(0), country).success.value
          .set(StartDatePage(0), date).success.value
          .set(AssetStatus(0), Completed).success.value

          .set(WhatKindOfAssetPage(1), NonEeaBusiness).success.value
          .set(NamePage(1), name).success.value
          .set(AddressUkYesNoPage(1), false).success.value
          .set(InternationalAddressPage(1), nonUkAddress).success.value
          .set(GoverningCountryPage(1), country).success.value
          .set(StartDatePage(1), date).success.value
          .set(AssetStatus(1), Completed).success.value

        val result = mapper.build(answers).get

        result mustBe List(
          NonEeaBusinessType(
            orgName = name,
            address = ukAddressType,
            govLawCountry = country,
            startDate = date
          ),
          NonEeaBusinessType(
            orgName = name,
            address = nonUkAddressType,
            govLawCountry = country,
            startDate = date
          )
        )
      }
    }
  }
}
