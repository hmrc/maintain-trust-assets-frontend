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

class AssetsNameTypeSpec extends AnyWordSpec with Matchers {

  case class NameTypeWithExpectedResult(assetNameType: AssetNameType, expectedToString: String)

  object TestClass extends AssetNameHelper

  "AssetsNameType.toString" must {
    "derive the expected asset name" in
      List(
        NameTypeWithExpectedResult(MoneyAssetNameType, "MoneyAsset"),
        NameTypeWithExpectedResult(PropertyOrLandAssetNameType, "PropertyOrLandAsset"),
        NameTypeWithExpectedResult(SharesAssetNameType, "SharesAsset"),
        NameTypeWithExpectedResult(BusinessAssetNameType, "BusinessAsset"),
        NameTypeWithExpectedResult(PartnershipAssetNameType, "PartnershipAsset"),
        NameTypeWithExpectedResult(OtherAssetNameType, "OtherAsset"),
        NameTypeWithExpectedResult(NonEeaBusinessAssetNameType, "NonEeaBusinessAsset")
      )
        .foreach(testCase => testCase.assetNameType.toString() mustEqual (testCase.expectedToString))

    "show the expected error string if the asset name could not be derived" in {
      TestClass.toString() mustEqual "[error: could not derive asset name]"
    }

  }

}
