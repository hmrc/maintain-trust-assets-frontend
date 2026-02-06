/*
 * Copyright 2026 HM Revenue & Customs
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

package viewmodels

import generators.{Generators, ModelGenerators}
import models.Status.{Completed, InProgress}
import models.WhatKindOfAsset._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

class AssetViewModelSpec
    extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with ModelGenerators {

  "Asset" - {

    "must deserialise" - {

      "money" - {

        "to a view model that is not complete" in {

          val json = Json.obj(
            "whatKindOfAsset" -> Money.toString
          )

          json.validate[AssetViewModel] mustEqual JsSuccess(
            MoneyAssetViewModel(Money, None, InProgress)
          )
        }

        "to a view model that is complete" in {
          val json = Json.obj(
            "whatKindOfAsset" -> Money.toString,
            "moneyValue"      -> 4000,
            "status"          -> Completed.toString
          )

          json.validate[AssetViewModel] mustEqual JsSuccess(
            MoneyAssetViewModel(Money, Some("4000"), Completed)
          )
        }

      }

      "shares" - {

        "to a view model that is not complete" - {

          "first question (are shares in a portfolio?) is unanswered" in {
            val json = Json.parse("""
                |{
                |"whatKindOfAsset" : "Shares",
                |"status": "progress"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              ShareAssetViewModel(Shares, None, InProgress)
            )
          }

          "name question is unanswered" in {
            val json = Json.parse("""
                |{
                |"whatKindOfAsset" : "Shares",
                |"status": "progress"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              ShareAssetViewModel(Shares, None, InProgress)
            )
          }
        }

        "to a view model that is complete" - {

          "shares are in a portfolio" in {
            val json = Json.parse("""
                |{
                |"portfolioSharesListedOnStockExchangeYesNo" : true,
                |"portfolioSharesName" : "adam",
                |"sharesInPortfolioYesNo" : true,
                |"portfolioSharesQuantity" : "200",
                |"portfolioSharesValue" : 200,
                |"whatKindOfAsset" : "Shares",
                |"status": "completed"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              ShareAssetViewModel(Shares, Some("adam"), Completed)
            )
          }

          "shares are not in a portfolio" in {
            val json = Json.parse("""
                |{
                |"nonPortfolioSharesListedOnStockExchangeYesNo" : true,
                |"nonPortfolioSharesName" : "adam",
                |"sharesInPortfolioYesNo" : false,
                |"nonPortfolioSharesQuantity" : "200",
                |"nonPortfolioSharesValue" : 200,
                |"whatKindOfAsset" : "Shares",
                |"nonPortfolioSharesClass" : "ordinary",
                |"status": "completed"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              ShareAssetViewModel(Shares, Some("adam"), Completed)
            )
          }
        }

      }

      "business" - {

        "with uk address" - {

          "to a view model that is not complete" in {
            val json = Json.parse("""
                |{
                |"businessName": "Business Ltd",
                |"businessDescription": "Some description",
                |"businessAddressUkYesNo": true,
                |"whatKindOfAsset" : "Business",
                |"status": "progress"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              BusinessAssetViewModel(Business, Some("Business Ltd"), InProgress)
            )
          }

          "to a view model that is complete" in {
            val json = Json.parse("""
                |{
                |"businessName": "Business Ltd",
                |"businessDescription": "Some description",
                |"businessAddressUkYesNo": true,
                |"businessUkAddress": {
                | "line1": "line 1",
                | "line2": "Newcastle",
                | "postCode": "NE11TU"
                |},
                |"whatKindOfAsset" : "Business",
                |"status": "completed"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              BusinessAssetViewModel(Business, Some("Business Ltd"), Completed)
            )
          }

        }
        "with international address" - {

          "to a view model that is not complete" in {
            val json = Json.parse("""
                |{
                |"businessName": "Business Ltd",
                |"businessDescription": "Some description",
                |"businessAddressUkYesNo": false,
                |"whatKindOfAsset" : "Business",
                |"status": "progress"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              BusinessAssetViewModel(Business, Some("Business Ltd"), InProgress)
            )
          }

          "to a view model that is complete" in {
            val json = Json.parse("""
                |{
                |"businessName": "Business Ltd",
                |"businessDescription": "Some description",
                |"businessAddressUkYesNo": false,
                |"businessInternationalAddress": {
                | "line1": "line 1",
                | "line2": "line 2",
                | "country": "France"
                |},
                |"whatKindOfAsset" : "Business",
                |"status": "completed"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              BusinessAssetViewModel(Business, Some("Business Ltd"), Completed)
            )
          }
        }
      }

      "property or land" - {

        "property or land with description" - {

          "to a view model that is not complete" in {
            val json = Json.parse("""
                |{
                |"propertyOrLandAddressYesNo": false,
                |"whatKindOfAsset" : "PropertyOrLand",
                |"status": "progress"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              PropertyOrLandAssetViewModel(PropertyOrLand, Some(false), None, None, InProgress)
            )
          }

          "to a view model that is complete" in {
            val json = Json.parse("""
                |{
                |"propertyOrLandAddressYesNo": false,
                |"propertyOrLandDescription": "1 hectare",
                |"whatKindOfAsset" : "PropertyOrLand",
                |"status": "completed"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              PropertyOrLandAssetViewModel(PropertyOrLand, Some(false), None, Some("1 hectare"), Completed)
            )
          }

        }

        "property or land with address" - {

          "uk address" - {

            "to a view model that is not complete" in {
              val json = Json.parse("""
                  |{
                  |"propertyOrLandAddressYesNo": true,
                  |"propertyOrLandAddressUkYesNo": true,
                  |"whatKindOfAsset" : "PropertyOrLand",
                  |"status": "progress"
                  |}
            """.stripMargin)

              json.validate[AssetViewModel] mustEqual JsSuccess(
                PropertyOrLandAssetViewModel(PropertyOrLand, Some(true), None, None, InProgress)
              )
            }

            "to a view model that is complete" in {
              val json = Json.parse("""
                  |{
                  |"propertyOrLandAddressYesNo": true,
                  |"propertyOrLandAddressUkYesNo": true,
                  |"propertyOrLandUkAddress": {
                  | "line1": "line 1",
                  | "line2": "Newcastle",
                  | "postCode": "NE11TU"
                  |},
                  |"whatKindOfAsset" : "PropertyOrLand",
                  |"status": "completed"
                  |}
            """.stripMargin)

              json.validate[AssetViewModel] mustEqual JsSuccess(
                PropertyOrLandAssetViewModel(PropertyOrLand, Some(true), Some("line 1"), None, Completed)
              )
            }

          }
          "international address" - {

            "to a view model that is not complete" in {
              val json = Json.parse("""
                  |{
                  |"propertyOrLandAddressYesNo": true,
                  |"propertyOrLandAddressUkYesNo": false,
                  |"whatKindOfAsset" : "PropertyOrLand",
                  |"status": "progress"
                  |}
            """.stripMargin)

              json.validate[AssetViewModel] mustEqual JsSuccess(
                PropertyOrLandAssetViewModel(PropertyOrLand, Some(true), None, None, InProgress)
              )
            }

            "to a view model that is complete" in {
              val json = Json.parse("""
                  |{
                  |"propertyOrLandAddressYesNo": true,
                  |"propertyOrLandAddressUkYesNo": false,
                  |"propertyOrLandInternationalAddress": {
                  | "line1": "line 1",
                  | "line2": "line 2",
                  | "country": "France"
                  |},
                  |"whatKindOfAsset" : "PropertyOrLand",
                  |"status": "completed"
                  |}
            """.stripMargin)

              json.validate[AssetViewModel] mustEqual JsSuccess(
                PropertyOrLandAssetViewModel(PropertyOrLand, Some(true), Some("line 1"), None, Completed)
              )
            }

          }
          "address" - {

            "to a view model that is not complete" in {
              val json = Json.parse("""
                  |{
                  |"propertyOrLandAddressYesNo": true,
                  |"whatKindOfAsset": "PropertyOrLand",
                  |"status": "progress"
                  |}
            """.stripMargin)

              json.validate[AssetViewModel] mustEqual JsSuccess(
                PropertyOrLandAssetViewModel(PropertyOrLand, Some(true), None, None, InProgress)
              )
            }

          }

        }

        "to default view model when no data provided" in {
          val json = Json.parse("""
              |{
              |"whatKindOfAsset" : "PropertyOrLand"
              |}
            """.stripMargin)

          json.validate[AssetViewModel] mustEqual JsSuccess(
            PropertyOrLandAssetViewModel(PropertyOrLand, None, None, None, InProgress)
          )
        }

      }

      "other" - {

        "to a view model that is not complete" in {

          val json = Json.obj(
            "whatKindOfAsset" -> Other.toString,
            "status"          -> InProgress.toString
          )

          json.validate[AssetViewModel] mustEqual JsSuccess(
            OtherAssetViewModel(Other, None, InProgress)
          )
        }

        "to a view model that is complete" in {
          val json = Json.obj(
            "whatKindOfAsset"  -> Other.toString,
            "otherDescription" -> "Description",
            "otherValue"       -> 4000,
            "status"           -> Completed.toString
          )

          json.validate[AssetViewModel] mustEqual JsSuccess(
            OtherAssetViewModel(Other, Some("Description"), Completed)
          )
        }

      }

      "partnership" - {

        "to a view model that is not complete" in {

          val json = Json.obj(
            "whatKindOfAsset" -> Partnership.toString,
            "status"          -> InProgress.toString
          )

          json.validate[AssetViewModel] mustEqual JsSuccess(
            PartnershipAssetViewModel(Partnership, None, InProgress)
          )
        }

        "to a view model that is complete" in {
          val json = Json.obj(
            "whatKindOfAsset"        -> Partnership.toString,
            "partnershipDescription" -> "Description",
            "status"                 -> Completed.toString
          )

          json.validate[AssetViewModel] mustEqual JsSuccess(
            PartnershipAssetViewModel(Partnership, Some("Description"), Completed)
          )
        }
      }

      "non-EEA business" - {

        "with no name" - {

          "to a view model that is not complete" in {
            val json = Json.parse("""
                |{
                |  "whatKindOfAsset" : "NonEeaBusiness",
                |  "status": "progress"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              NonEeaBusinessAssetViewModel(NonEeaBusiness, None, InProgress)
            )
          }
        }

        "with uk address" - {

          "to a view model that is not complete" in {
            val json = Json.parse("""
                |{
                |  "nonEeaBusinessName": "Business Ltd",
                |  "nonEeaBusinessAddressUkYesNo": true,
                |  "whatKindOfAsset" : "NonEeaBusiness",
                |  "status": "progress"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              NonEeaBusinessAssetViewModel(NonEeaBusiness, Some("Business Ltd"), InProgress)
            )
          }

          "to a view model that is complete" in {
            val json = Json.parse("""
                |{
                |  "nonEeaBusinessName": "Business Ltd",
                |  "nonEeaBusinessAddressUkYesNo": true,
                |  "nonEeaBusinessUkAddress": {
                |    "line1": "line 1",
                |    "line2": "Line 2",
                |    "postCode": "AB1 1AB"
                |  },
                |  "nonEeaBusinessGoverningCountry": "GB",
                |  "nonEeaBusinessStartDate": "1996-02-03",
                |  "whatKindOfAsset": "NonEeaBusiness",
                |  "status": "completed"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              NonEeaBusinessAssetViewModel(NonEeaBusiness, Some("Business Ltd"), Completed)
            )
          }

        }

        "with international address" - {

          "to a view model that is not complete" in {
            val json = Json.parse("""
                |{
                |  "nonEeaBusinessName": "Business Ltd",
                |  "nonEeaBusinessAddressUkYesNo": false,
                |  "whatKindOfAsset": "NonEeaBusiness",
                |  "status": "progress"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              NonEeaBusinessAssetViewModel(NonEeaBusiness, Some("Business Ltd"), InProgress)
            )
          }

          "to a view model that is complete" in {
            val json = Json.parse("""
                |{
                |  "nonEeaBusinessName": "Business Ltd",
                |  "nonEeaBusinessAddressUkYesNo": false,
                |  "nonEeaBusinessInternationalAddress": {
                |    "line1": "line 1",
                |    "line2": "line 2",
                |    "country": "FR"
                |  },
                |  "nonEeaBusinessGoverningCountry": "FR",
                |  "nonEeaBusinessStartDate": "1996-02-03",
                |  "whatKindOfAsset": "NonEeaBusiness",
                |  "status": "completed"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              NonEeaBusinessAssetViewModel(NonEeaBusiness, Some("Business Ltd"), Completed)
            )
          }
        }
      }
    }
  }

}
