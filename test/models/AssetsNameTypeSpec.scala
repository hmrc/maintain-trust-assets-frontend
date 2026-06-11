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

package models

import models.assets.AssetNameType._
import models.assets._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsString, Json}

class AssetsNameTypeSpec extends AnyWordSpec with Matchers {

  "AssetsNameType.toString" must {
    "derive the expected asset name" in
      List(
        (MoneyAssetNameType, "MoneyAsset"),
        (PropertyOrLandAssetNameType, "PropertyOrLandAsset"),
        (SharesAssetNameType, "SharesAsset"),
        (BusinessAssetNameType, "BusinessAsset"),
        (PartnershipAssetNameType, "PartnershipAsset"),
        (OtherAssetNameType, "OtherAsset"),
        (NonEeaBusinessAssetNameType, "NonEeaBusinessAsset")
      )
        .foreach { testCase =>
          val assetNameType  = testCase._1
          val expectedResult = testCase._2

          assetNameType.toString() mustEqual expectedResult
        }
  }

  "AssetsNameType.writesToTrusts" must {

    "serialise each asset name type to the value expected by the trusts backend" in
      List[(AssetNameType, String)](
        (MoneyAssetNameType, "monetary"),
        (PropertyOrLandAssetNameType, "propertyOrLand"),
        (SharesAssetNameType, "shares"),
        (BusinessAssetNameType, "business"),
        (PartnershipAssetNameType, "partnerShip"),
        (OtherAssetNameType, "other"),
        (NonEeaBusinessAssetNameType, "nonEEABusiness")
      )
        .foreach { testCase =>
          val assetNameType  = testCase._1
          val expectedResult = testCase._2

          Json.toJson(assetNameType)(AssetNameType.writesToTrusts) mustEqual JsString(expectedResult)
        }
  }

}
