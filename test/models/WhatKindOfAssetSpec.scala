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

import models.Status._
import models.WhatKindOfAsset._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}
import viewmodels._

class WhatKindOfAssetSpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  "WhatKindOfAsset" must {

    "deserialise valid values" in {

      val gen = Gen.oneOf(WhatKindOfAsset.values)

      forAll(gen) {
        whatKindOfAsset =>

          JsString(whatKindOfAsset.toString).validate[WhatKindOfAsset].asOpt.value mustEqual whatKindOfAsset
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!WhatKindOfAsset.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[WhatKindOfAsset] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = Gen.oneOf(WhatKindOfAsset.values)

      forAll(gen) {
        whatKindOfAsset =>

          Json.toJson(whatKindOfAsset) mustEqual JsString(whatKindOfAsset.toString)
      }
    }

    "return the non maxed out options" when {

      "no assets" in {

        val assets: List[AssetViewModel] = Nil

        WhatKindOfAsset.nonMaxedOutOptions(assets, assetTypeAtIndex = None) mustBe List(
          RadioOption("whatKindOfAsset", Money.toString),
          RadioOption("whatKindOfAsset", PropertyOrLand.toString),
          RadioOption("whatKindOfAsset", Shares.toString),
          RadioOption("whatKindOfAsset", Business.toString),
          RadioOption("whatKindOfAsset", Partnership.toString),
          RadioOption("whatKindOfAsset", Other.toString),
          RadioOption("whatKindOfAsset", NonEeaBusiness.toString)
        )

      }

      "there is a 'Money' asset" when {

        val moneyAsset = MoneyAssetViewModel(Money, Some("4000"), Completed)
        val assets: List[AssetViewModel] = List(moneyAsset)

        "at this index" in {

          WhatKindOfAsset.nonMaxedOutOptions(assets, assetTypeAtIndex = Some(Money)) mustBe List(
            RadioOption("whatKindOfAsset", Money.toString),
            RadioOption("whatKindOfAsset", PropertyOrLand.toString),
            RadioOption("whatKindOfAsset", Shares.toString),
            RadioOption("whatKindOfAsset", Business.toString),
            RadioOption("whatKindOfAsset", Partnership.toString),
            RadioOption("whatKindOfAsset", Other.toString),
            RadioOption("whatKindOfAsset", NonEeaBusiness.toString)
          )
        }

        "at a different index" in {

          WhatKindOfAsset.nonMaxedOutOptions(assets, assetTypeAtIndex = None) mustBe List(
            RadioOption("whatKindOfAsset", PropertyOrLand.toString),
            RadioOption("whatKindOfAsset", Shares.toString),
            RadioOption("whatKindOfAsset", Business.toString),
            RadioOption("whatKindOfAsset", Partnership.toString),
            RadioOption("whatKindOfAsset", Other.toString),
            RadioOption("whatKindOfAsset", NonEeaBusiness.toString)
          )
        }
      }

      "there are a combined 10 Completed and InProgress assets of a particular type that isn't 'Money'" when {

        "asset at this index is of that type" in {
          val otherAssetCompleted = OtherAssetViewModel(Other, Some("description"), Completed)
          val otherAssetInProgress = OtherAssetViewModel(Other, None, InProgress)

          val assets: List[AssetViewModel] = List.fill(5)(otherAssetCompleted) ++ List.fill(5)(otherAssetInProgress)

          WhatKindOfAsset.nonMaxedOutOptions(assets, assetTypeAtIndex = Some(Other)) mustBe List(
            RadioOption("whatKindOfAsset", Money.toString),
            RadioOption("whatKindOfAsset", PropertyOrLand.toString),
            RadioOption("whatKindOfAsset", Shares.toString),
            RadioOption("whatKindOfAsset", Business.toString),
            RadioOption("whatKindOfAsset", Partnership.toString),
            RadioOption("whatKindOfAsset", Other.toString),
            RadioOption("whatKindOfAsset", NonEeaBusiness.toString)
          )
        }

        "no asset at this index" in {
          val otherAssetCompleted = OtherAssetViewModel(Other, Some("description"), Completed)
          val otherAssetInProgress = OtherAssetViewModel(Other, None, InProgress)

          val assets: List[AssetViewModel] = List.fill(5)(otherAssetCompleted) ++ List.fill(5)(otherAssetInProgress)

          WhatKindOfAsset.nonMaxedOutOptions(assets, assetTypeAtIndex = None) mustBe List(
            RadioOption("whatKindOfAsset", Money.toString),
            RadioOption("whatKindOfAsset", PropertyOrLand.toString),
            RadioOption("whatKindOfAsset", Shares.toString),
            RadioOption("whatKindOfAsset", Business.toString),
            RadioOption("whatKindOfAsset", Partnership.toString),
            RadioOption("whatKindOfAsset", NonEeaBusiness.toString)
          )
        }
      }
    }
  }
}