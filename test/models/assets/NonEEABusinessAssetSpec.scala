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

package models.assets

import java.time.LocalDate

import models.{InternationalAddress, UKAddress}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json

class NonEEABusinessAssetSpec extends WordSpec with MustMatchers{


  "NonEEABusinessAsset" must {
    "deserialise from backend JSON" when {

      "there is a NonEEABusinessAsset with an InternationalAddress" in {
        val json = Json.parse(
          """
            |{
            |   "lineNo": "1",
            |   "orgName": "Panda care Ltd",
            |   "address": {
            |     "country": "CA",
            |     "line1": "1010 EASY ST",
            |     "line2": "OTTAWA",
            |     "line3": "ONTARIO",
            |     "line4": "ONTARIO"
            |   },
            |   "govLawCountry": "CA",
            |   "startDate": "2020-01-05"
            |}
            |""".stripMargin)

        val asset = json.as[NonEEABusinessAsset]

        val address = InternationalAddress(
          line1 = "1010 EASY ST",
          line2 = "OTTAWA",
          line3 = Some("ONTARIO"),
          country = "CA")

        asset mustBe NonEEABusinessAsset(
          lineNo = "1",
          orgName = "Panda care Ltd",
          address = Some(address),
          govLawCountry = "CA",
          startDate = LocalDate.of(2020, 1, 5)
        )
      }

      "there is a NonEEABusinessAsset with an UkAddress" in {
        val json = Json.parse(
          """
            |{
            |   "lineNo": "1",
            |   "orgName": "Panda care Ltd",
            |   "address": {
            |     "postcode": "AB1 1AB",
            |     "line1": "1010 EASY ST",
            |     "line2": "North Shields",
            |     "line3": "Tyne & Wear",
            |     "line4": "ENGLAND"
            |   },
            |   "govLawCountry": "CA",
            |   "startDate": "2020-01-05"
            |}
            |""".stripMargin)

        val asset = json.as[NonEEABusinessAsset]

        val address = UKAddress(
          line1 = "1010 EASY ST",
          line2 = "North Shields",
          line3 = Some("Tyne & Wear"),
          line4 = Some("ENGLAND"),
          postcode = "AB1 1AB")

        asset mustBe NonEEABusinessAsset(
          lineNo = "1",
          orgName = "Panda care Ltd",
          address = Some(address),
          govLawCountry = "CA",
          startDate = LocalDate.of(2020, 1, 5)
        )
      }
    }
  }
}
