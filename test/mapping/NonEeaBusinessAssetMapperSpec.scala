/*
 * Copyright 2024 HM Revenue & Customs
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
import models.NonUkAddress
import models.WhatKindOfAsset.NonEeaBusiness
import models.assets.NonEeaBusinessType
import pages.asset._
import pages.asset.noneeabusiness._
import pages.asset.noneeabusiness.add.StartDatePage

import java.time.LocalDate

class NonEeaBusinessAssetMapperSpec extends SpecBase {

  private val mapper: NonEeaBusinessAssetMapper = injector.instanceOf[NonEeaBusinessAssetMapper]

  private val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true)

  private val name: String = "Name"
  private val country: String = "FR"
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", Some("Line 3"), country)
  private val date: LocalDate = LocalDate.parse("1996-02-03")

  "NonEeaBusinessAssetMapper" must {

    "not be able to create a non-EEA business asset" when {

      "not all questions answered" in {

        val answers = baseAnswers

        mapper(answers).isDefined mustBe false
      }
    }


    "be able to create a non-EEA business asset" when {

      "one asset" in {

        val answers = baseAnswers
          .set(WhatKindOfAssetPage(index), NonEeaBusiness).success.value
          .set(NamePage(index), name).success.value
          .set(NonUkAddressPage(index), nonUkAddress).success.value
          .set(GoverningCountryPage(index), country).success.value
          .set(StartDatePage(index), date).success.value

        val result = mapper(answers).get

        result mustBe
          NonEeaBusinessType(lineNo = None, orgName = name, address = nonUkAddress, govLawCountry = country, startDate = date, endDate = None, provisional = true)
      }
    }
  }
}
