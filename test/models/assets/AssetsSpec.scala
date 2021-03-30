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

import models.InternationalAddress
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json

class AssetsSpec extends WordSpec with MustMatchers{


  "Assets" must {
    "deserialise from backend JSON" when {

      "we have the different Assets" in {
        val json = Json.parse(
          """
            | {
            |    "assets": {
            |        "monetary": [
            |          {
            |            "assetMonetaryAmount": 1000
            |          }
            |        ],
            |        "propertyOrLand": [
            |          {
            |            "valueFull": 1000
            |          }
            |        ],
            |        "shares": [
            |          {
            |            "orgName": "Shares Ltd"
            |          }
            |        ],
            |        "business": [
            |          {
            |            "orgName": "Business Ltd",
            |            "businessDescription": "Business description"
            |          }
            |        ],
            |        "partnerShip": [
            |          {
            |            "description": "Partnership description"
            |          }
            |        ],
            |        "other": [
            |          {
            |            "description": "Other description"
            |          }
            |        ],
            |        "nonEEABusiness": [
            |          {
            |            "lineNo": "1",
            |            "orgName": "Panda care Ltd",
            |            "address": {
            |              "country": "CA",
            |              "line1": "1010 EASY ST",
            |              "line2": "OTTAWA",
            |              "line3": "ONTARIO",
            |              "line4": "ONTARIO"
            |            },
            |            "govLawCountry": "CA",
            |            "startDate": "2020-01-05"
            |          }
            |        ]
            |      }
            | }
            |""".stripMargin)

        val assets = json.as[Assets]

        val monEEAAsset1 = NonEEABusinessAsset(
          lineNo = "1",
          orgName = "Panda care Ltd",
          address = Some(InternationalAddress(
            line1 = "1010 EASY ST",
            line2 = "OTTAWA",
            line3 = Some("ONTARIO"),
            country = "CA")),
          govLawCountry = "CA",
          startDate = LocalDate.of(2020, 1, 5)
        )
        val businessAsset = BusinessAsset(
          orgName = "Business Ltd",
          description = "Business description"
        )
        assets mustBe Assets(
          businessAssets = List(businessAsset),
          monetaryAssets = List(MonetaryAsset(1000)),
          nonEEABusinessAssets = List(monEEAAsset1),
          partnershipAssets = List(PartnershipAsset("Partnership description")),
          propertyOrLandAssets = List(PropertyOrLandAsset(1000)),
          sharesAssets = List(SharesAsset("Shares Ltd")),
          otherAssets = List(OtherAsset("Other description"))
        )
      }
    }
  }
}
