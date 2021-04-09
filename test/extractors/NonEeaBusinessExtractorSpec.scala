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

package extractors

import java.time.LocalDate

import base.SpecBase
import models.assets.NonEeaBusinessType
import models.{NonUkAddress, UserAnswers}
import pages.asset.noneeabusiness.add.StartDatePage
import pages.asset.noneeabusiness.amend.IndexPage
import pages.asset.noneeabusiness.{GoverningCountryPage, NamePage, NonUkAddressPage}

class NonEeaBusinessExtractorSpec extends SpecBase {

  private val index = 0
  private val name: String = "OrgName"
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val country: String = "FR"
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "FR")

  private val extractor = new NonEeaBusinessExtractor()

  "NonEeaBusinessExtractor" must {

    "Populate user answers" when {

      "5mld" when {

        val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = false)

        "has nonEeaBusiness asset data" in {

          val nonEeaBusiness = NonEeaBusinessType(
            lineNo = None,
            orgName = name,
            address = nonUkAddress,
            govLawCountry = country,
            startDate = date,
            endDate = None,
            provisional = true
          )

          val result = extractor(baseAnswers, nonEeaBusiness, index).get

          result.get(IndexPage).get mustBe index
          result.get(NamePage).get mustBe name
          result.get(NonUkAddressPage).get mustBe nonUkAddress
          result.get(GoverningCountryPage).get mustBe country
          result.get(StartDatePage).get mustBe date
        }

      }

    }

  }

}
