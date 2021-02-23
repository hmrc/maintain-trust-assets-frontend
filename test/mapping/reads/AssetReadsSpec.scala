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

package mapping.reads

import models.WhatKindOfAsset.Money
import models.{InternationalAddress, ShareClass, UKAddress, WhatKindOfAsset}
import org.scalatest.{FreeSpec, MustMatchers}
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.time.LocalDate

class AssetReadsSpec extends FreeSpec with MustMatchers {

  "Asset" - {

    "must fail to deserialise" - {

      "a money asset of the incorrect structure" in {
        val json = Json.obj(
          "whatKindOfAsset" -> "Property",
          "moneyValue" -> 4000
        )

        json.validate[Asset] mustBe a[JsError]
      }

      "a non-portfolio share asset of the incorrect structure" in {
        val json = Json.parse(
          """
            |{
            |"nonPortfolioSharesListedOnStockExchangeYesNo" : true,
            |"nonPortfolioSharesName" : "adam",
            |"sharesInPortfolioYesNo" : false,
            |"nonPortfolioSharesQuantity" : "200",
            |"nonPortfolioSharesValue" : 200,
            |"whatKindOfAsset" : "Shares"
            |}
          """.stripMargin)

        json.validate[Asset] mustBe a[JsError]

      }

      "a portfolio share asset of the incorrect structure" in {
        val json = Json.parse(
          """
            |{
            |"portfolioSharesListedOnStockExchangeYesNo" : true,
            |"sharesInPortfolioYesNo" : true,
            |"portfolioSharesValue" : 290000,
            |"whatKindOfAsset" : "Shares",
            |"status" : "progress"
            |}
          """.stripMargin)

        json.validate[Asset] mustBe a[JsError]
      }

      "a business asset of the incorrect structure" in {
        val json = Json.parse(
          """
            |{
            |"whatKindOfAsset" : "Business",
            |"businessName": "Business Ltd",
            |"businessUkAddress" : {
            |     "line1" : "26",
            |     "line2" : "Grangetown",
            |     "line3" : "Tyne and Wear",
            |     "line4" : "Newcastle",
            |     "postcode" : "Z99 2YY"
            |},
            |"businessValue" : 75
            |}
          """.stripMargin)

        json.validate[Asset] mustBe a[JsError]
      }

      "a property or land asset of the incorrect structure" in {
        val json = Json.parse(
          """
            |{
            |"whatKindOfAsset" : "PropertyOrLand",
            |"propertyOrLandDescription" : "Property Or Land",
            |"propertyOrLandUkAddress" : {
            |     "line1" : "26",
            |     "line2" : "Grangetown",
            |     "line3" : "Tyne and Wear",
            |     "line4" : "Newcastle",
            |     "postcode" : "Z99 2YY"
            |},
            |"propertyOrLandValueInTrust" : 75
            |}
          """.stripMargin)

        json.validate[Asset] mustBe a[JsError]
      }

      "a partnership asset of the incorrect structure" in {
        val json = Json.obj(
          "whatKindOfAsset" -> "Partnership",
          "partnershipDescription" -> "Description"
        )

        json.validate[Asset] mustBe a[JsError]
      }

      "an other asset of the incorrect structure" in {
        val json = Json.obj(
          "whatKindOfAsset" -> "Other",
          "otherDescription" -> "Description"
        )

        json.validate[Asset] mustBe a[JsError]
      }

      "a non-EEA business asset of the incorrect structure" in {
        val json = Json.obj(
          "whatKindOfAsset" -> "NonEeaBusiness",
          "nonEeaBusinessName" -> "Name"
        )

        json.validate[Asset] mustBe a[JsError]
      }
    }

    "must deserialise" - {

      "a money asset" in {
        val json = Json.obj(
          "whatKindOfAsset" -> "Money",
          "moneyValue" -> 4000
        )

        json.validate[Asset] mustEqual JsSuccess(MoneyAsset(Money, 4000L))
      }

      "a non-portfolio share asset" in {

        val json = Json.parse(
          """
            |{
            |"nonPortfolioSharesOnStockExchangeYesNo" : true,
            |"nonPortfolioSharesName" : "adam",
            |"sharesInPortfolioYesNo" : false,
            |"nonPortfolioSharesQuantity" : 100,
            |"nonPortfolioSharesValue" : 200,
            |"whatKindOfAsset" : "Shares",
            |"nonPortfolioSharesClass" : "ordinary",
            |"status": "completed"
            |}
          """.stripMargin)

        json.validate[Asset] mustEqual JsSuccess(
          ShareNonPortfolioAsset(whatKindOfAsset = WhatKindOfAsset.Shares, sharesInAPortfolio = false, name = "adam", listedOnTheStockExchange = true, `class` = ShareClass.Ordinary, quantityInTheTrust = 100L, value = 200L))

      }

      "a portfolio share asset" in {
        val json = Json.parse(
          """
            |{
            |"portfolioSharesOnStockExchangeYesNo" : true,
            |"sharesInPortfolioYesNo" : true,
            |"portfolioSharesName" : "Adam",
            |"portfolioSharesQuantity" : 200,
            |"portfolioSharesValue" : 290000,
            |"whatKindOfAsset" : "Shares",
            |"status" : "completed"
            |}
          """.stripMargin)

        json.validate[Asset] mustEqual JsSuccess(
          SharePortfolioAsset(whatKindOfAsset = WhatKindOfAsset.Shares, sharesInAPortfolio = true, name = "Adam", listedOnTheStockExchange = true, quantityInTheTrust = 200L, value = 290000L))
      }

      "a business asset" in {
        val json = Json.parse(
          """
            |{
            |"whatKindOfAsset" : "Business",
            |"businessName": "Business Ltd",
            |"businessDescription": "Some description",
            |"businessAddressUkYesNo": false,
            |"businessUkAddress" : {
            |     "line1" : "26",
            |     "line2" : "Grangetown",
            |     "line3" : "Tyne and Wear",
            |     "line4" : "Newcastle",
            |     "postcode" : "Z99 2YY"
            |},
            |"businessValue" : 75
            |}
          """.stripMargin)

        json.validate[Asset] mustEqual JsSuccess(
          BusinessAsset(
            whatKindOfAsset = WhatKindOfAsset.Business,
            assetName = "Business Ltd",
            assetDescription = "Some description",
            address = UKAddress(
              line1 = "26",
              line2 = "Grangetown",
              line3 = Some("Tyne and Wear"),
              line4 = Some("Newcastle"),
              postcode = "Z99 2YY"
            ),
            currentValue = 75L
          ))
      }

      "a property or land asset" in {
        val json = Json.parse(
          """
            |{
            |"whatKindOfAsset" : "PropertyOrLand",
            |"propertyOrLandDescription" : "Property Or Land",
            |"propertyOrLandUkAddress" : {
            |     "line1" : "26",
            |     "line2" : "Grangetown",
            |     "line3" : "Tyne and Wear",
            |     "line4" : "Newcastle",
            |     "postcode" : "Z99 2YY"
            |},
            |"propertyOrLandValueInTrust" : 75,
            |"propertyOrLandTotalValue" : 1000
            |}
          """.stripMargin)

        json.validate[Asset] mustEqual JsSuccess(
          PropertyOrLandAsset(
            whatKindOfAsset = WhatKindOfAsset.PropertyOrLand,
            propertyOrLandDescription = Some("Property Or Land"),
            address = Some(
              UKAddress(
                line1 = "26",
                line2 = "Grangetown",
                line3 = Some("Tyne and Wear"),
                line4 = Some("Newcastle"),
                postcode = "Z99 2YY"
              )),
            propertyLandValueTrust = Some(75L),
            propertyOrLandTotalValue = 1000L
          ))
      }

      "a property or land asset with minimum data" in {
        val json = Json.parse(
          """
            |{
            |"whatKindOfAsset" : "PropertyOrLand",
            |"propertyOrLandUkAddress" : {
            |     "line1" : "26",
            |     "line2" : "Newcastle",
            |     "postcode" : "Z99 2YY"
            |},
            |"propertyOrLandTotalValue" : 1000
            |}
          """.stripMargin)

        json.validate[Asset] mustEqual JsSuccess(
          PropertyOrLandAsset(
            whatKindOfAsset = WhatKindOfAsset.PropertyOrLand,
            propertyOrLandDescription = None,
            address = Some(
              UKAddress(
                line1 = "26",
                line2 = "Newcastle",
                line3 = None,
                line4 = None,
                postcode = "Z99 2YY"
              )),
            propertyLandValueTrust = None,
            propertyOrLandTotalValue = 1000L
          ))
      }

      "a partnership asset" in {
        val json = Json.obj(
          "whatKindOfAsset" -> "Partnership",
          "partnershipDescription" -> "Description",
          "partnershipStartDate" -> "1996-02-03"
        )

        json.validate[Asset] mustEqual JsSuccess(
          PartnershipAsset(
            whatKindOfAsset = WhatKindOfAsset.Partnership,
            description = "Description",
            startDate = LocalDate.parse("1996-02-03")
          )
        )
      }

      "an other asset" in {
        val json = Json.obj(
          "whatKindOfAsset" -> "Other",
          "otherDescription" -> "Description",
          "otherValue" -> 4000
        )

        json.validate[Asset] mustBe JsSuccess(
          OtherAsset(
            whatKindOfAsset = WhatKindOfAsset.Other,
            description = "Description",
            value = 4000L
          )
        )
      }

      "a non-EEA business asset" in {
        val json = Json.parse(
          """
            |{
            |  "whatKindOfAsset": "NonEeaBusiness",
            |  "nonEeaBusinessName": "Name",
            |  "nonEeaBusinessInternationalAddress": {
            |     "line1": "21 Test Lane",
            |     "line2": "Test Town",
            |     "country": "FR"
            |  },
            |  "nonEeaBusinessGoverningCountry": "GB",
            |  "nonEeaBusinessStartDate": "1996-02-03"
            |}
          """.stripMargin)

        json.validate[Asset] mustBe JsSuccess(
          NonEeaBusinessAsset(
            whatKindOfAsset = WhatKindOfAsset.NonEeaBusiness,
            name = "Name",
            address = InternationalAddress("21 Test Lane", "Test Town", None, "FR"),
            governingCountry = "GB",
            startDate = LocalDate.parse("1996-02-03")
          )
        )
      }
    }
  }
}
