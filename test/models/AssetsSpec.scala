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

package models

import java.time.LocalDate

import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.Json

class AssetsSpec extends WordSpec with MustMatchers{


  "Assets" must {
    "deserialise from backend JSON" when {

      "we have the monetary Assets" in {
        val json = Json.parse(
          """
            | {
            |    "assets": {
            |        "monetary": [
            |          {
            |            "assetMonetaryAmount": 1000
            |          }
            |        ]
            |     }
            | }
            |""".stripMargin)

        val assets = json.as[Assets]

        assets mustBe Assets(monetary = List(AssetMonetaryAmount(1000)),
          propertyOrLand = Nil,
          shares = Nil,
          business = Nil,
          partnerShip = Nil,
          other = Nil,
          nonEEABusiness = Nil)
      }

      "we have the propertyOrLand Assets" in {
        val json = Json.parse(
          """
            | {
            |    "assets": {
            |        "propertyOrLand": [
            |          {
            |            "buildingLandName": "PropertyOrLand Name",
            |            "valueFull": 1000,
            |            "valuePrevious": 500
            |          }
            |        ]
            |    }
            | }
            |""".stripMargin)

        val assets = json.as[Assets]

        assets mustBe Assets(monetary = Nil,
          propertyOrLand = List(PropertyLandType(Some("PropertyOrLand Name"), None, 1000, Some(500))),
          shares = Nil,
          business = Nil,
          partnerShip = Nil,
          other = Nil,
          nonEEABusiness = Nil)
      }

      "we have the shares Assets" in {
        val json = Json.parse(
          """
            | {
            |    "assets": {
            |        "shares": [
            |          {
            |            "numberOfShares": "999999999999",
            |            "orgName": "Shares Ltd",
            |            "shareClass": "Other",
            |            "typeOfShare": "Unquoted",
            |            "value": 999999999999
            |          }
            |        ]
            |      }
            | }
            |""".stripMargin)

        val assets = json.as[Assets]

        assets mustBe Assets(monetary = Nil,
          propertyOrLand = Nil,
          shares =  List(SharesType("999999999999", "Shares Ltd", "Other", "Unquoted", 999999999999L)),
          business = Nil,
          partnerShip = Nil,
          other = Nil,
          nonEEABusiness = Nil
        )
      }

      "we have the business Assets" in {
        val json = Json.parse(
          """
            | {
            |    "assets": {
            |        "business": [
            |          {
            |            "address": {
            |              "line1": "123 Test Street",
            |              "line2": "Test Village",
            |              "line3": "Test line3",
            |              "line4": "Test line4",
            |              "postCode": "Z99 2YY",
            |              "country": "GB"
            |            },
            |            "orgName": "Business Ltd",
            |            "businessDescription": "Business description",
            |            "businessValue": 1000
            |          }
            |        ]
             |      }
            | }
            |""".stripMargin)

        val assets = json.as[Assets]

        val businessAsset = BusinessAssetType(
          orgName = "Business Ltd",
          businessDescription = "Business description",
          address = AddressType(
            line1 = "123 Test Street",
            line2 = "Test Village",
            line3 = Some("Test line3"),
            line4 = Some("Test line4"),
            postCode = Some("Z99 2YY"),
            country = "GB"),
          businessValue = 1000L
        )

        assets mustBe Assets(monetary = Nil,
          propertyOrLand = Nil,
          shares = Nil,
          business = List(businessAsset),
          partnerShip = Nil,
          other = Nil,
          nonEEABusiness = Nil
        )
      }


      "we have the other Assets" in {
        val json = Json.parse(
          """
            | {
            |    "assets": {
            |        "other": [
            |          {
            |            "description": "Other description",
            |            "value": 999999999999
            |          }
            |        ]
            |      }
            | }
            |""".stripMargin)

        val assets = json.as[Assets]

        assets mustBe Assets(monetary = Nil,
          propertyOrLand = Nil,
          shares = Nil,
          business = Nil,
          partnerShip = Nil,
          other = List(OtherAssetType("Other description", 999999999999L)),
          nonEEABusiness = Nil
        )
      }

      "we have the partnership Assets" in {
        val json = Json.parse(
          """
            | {
            |    "assets": {
            |        "partnerShip": [
            |          {
            |            "description": "Partnership description",
            |            "partnershipStart": "2015-03-20"
            |          }
            |        ]
            |    }
            | }
            |""".stripMargin)

        val assets = json.as[Assets]

        assets mustBe Assets(monetary = Nil,
          propertyOrLand = Nil,
          shares = Nil,
          business = Nil,
          partnerShip = List(PartnershipType("Partnership description", LocalDate.of(2015, 3, 20))),
          other = Nil,
          nonEEABusiness = Nil
        )
      }

      "we have the nonEEABusiness Assets" in {
        val json = Json.parse(
          """
            | {
            |    "assets": {
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

        val monEEAAsset1 = NonEeaBusinessType(
            lineNo = Some("1"),
            orgName = "Panda care Ltd",
            address = AddressType(
              line1 = "1010 EASY ST",
              line2 = "OTTAWA",
              line3 = Some("ONTARIO"),
              line4 = Some("ONTARIO"),
              postCode = None,
              country = "CA"),
          govLawCountry = "CA",
          startDate = LocalDate.of(2020, 1, 5),
          endDate = None
        )
        assets mustBe Assets(monetary = Nil,
          propertyOrLand = Nil,
          shares = Nil,
          business = Nil,
          partnerShip = Nil,
          other = Nil,
          nonEEABusiness = List(monEEAAsset1)
        )
      }
    }
  }
}
