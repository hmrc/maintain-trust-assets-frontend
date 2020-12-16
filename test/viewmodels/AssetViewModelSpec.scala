/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

class AssetViewModelSpec extends FreeSpec with MustMatchers with ScalaCheckPropertyChecks with Generators with ModelGenerators {

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
            "assetMoneyValue" -> 4000,
            "status" -> Completed.toString
          )

          json.validate[AssetViewModel] mustEqual JsSuccess(
            MoneyAssetViewModel(Money, Some("4000"), Completed)
          )
        }

      }

      "shares" - {

        "to a view model that is not complete" - {

          "first question (are shares in a portfolio?) is unanswered" in {
            val json = Json.parse(
              """
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
            val json = Json.parse(
              """
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
            val json = Json.parse(
              """
                |{
                |"portfolioListedOnStockExchange" : true,
                |"name" : "adam",
                |"sharesInAPortfolio" : true,
                |"portfolioQuantity" : "200",
                |"portfolioValue" : 200,
                |"whatKindOfAsset" : "Shares",
                |"status": "completed"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              ShareAssetViewModel(Shares, Some("adam"), Completed)
            )
          }

          "shares are not in a portfolio" in {
            val json = Json.parse(
              """
                |{
                |"listedOnStockExchange" : true,
                |"name" : "adam",
                |"sharesInAPortfolio" : false,
                |"quantity" : "200",
                |"value" : 200,
                |"whatKindOfAsset" : "Shares",
                |"class" : "ordinary",
                |"status": "completed"
                |}
            """.stripMargin)

            json.validate[AssetViewModel] mustEqual JsSuccess(
              ShareAssetViewModel(Shares, Some("adam"), Completed)
            )
          }
        }

      }

      "property or land" - {

        "property or land with description" - {

          "to a view model that is not complete" in {
            val json = Json.parse(
              """
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
            val json = Json.parse(
              """
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
              val json = Json.parse(
                """
                  |{
                  |"propertyOrLandAddressYesNo": true,
                  |"propertyOrLandAddressUKYesNo": true,
                  |"whatKindOfAsset" : "PropertyOrLand",
                  |"status": "progress"
                  |}
            """.stripMargin)

              json.validate[AssetViewModel] mustEqual JsSuccess(
                PropertyOrLandAssetViewModel(PropertyOrLand, Some(true), None, None, InProgress)
              )
            }

            "to a view model that is complete" in {
              val json = Json.parse(
                """
                  |{
                  |"propertyOrLandAddressYesNo": true,
                  |"propertyOrLandAddressUKYesNo": true,
                  |"ukAddress": {
                  | "line1": "line 1",
                  | "line2": "Newcastle",
                  | "postcode": "NE11TU"
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
              val json = Json.parse(
                """
                  |{
                  |"propertyOrLandAddressYesNo": true,
                  |"propertyOrLandAddressUKYesNo": false,
                  |"whatKindOfAsset" : "PropertyOrLand",
                  |"status": "progress"
                  |}
            """.stripMargin)

              json.validate[AssetViewModel] mustEqual JsSuccess(
                PropertyOrLandAssetViewModel(PropertyOrLand, Some(true), None, None, InProgress)
              )
            }

            "to a view model that is complete" in {
              val json = Json.parse(
                """
                  |{
                  |"propertyOrLandAddressYesNo": true,
                  |"propertyOrLandAddressUKYesNo": false,
                  |"internationalAddress": {
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
              val json = Json.parse(
                """
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
          val json = Json.parse(
            """
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
            "status" -> InProgress.toString
          )

          json.validate[AssetViewModel] mustEqual JsSuccess(
            OtherAssetViewModel(Other, None, InProgress)
          )
        }

        "to a view model that is complete" in {
          val json = Json.obj(
            "whatKindOfAsset" -> Other.toString,
            "otherAssetDescription" -> "Description",
            "otherAssetValue" -> 4000,
            "status" -> Completed.toString
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
            "status" -> InProgress.toString
          )

          json.validate[AssetViewModel] mustEqual JsSuccess(
            PartnershipAssetViewModel(Partnership, None, InProgress)
          )
        }

        "to a view model that is complete" in {
          val json = Json.obj(
            "whatKindOfAsset" -> Partnership.toString,
            "partnershipDescription" -> "Description",
            "status" -> Completed.toString
          )

          json.validate[AssetViewModel] mustEqual JsSuccess(
            PartnershipAssetViewModel(Partnership, Some("Description"), Completed)
          )
        }

      }
    }
  }

}
