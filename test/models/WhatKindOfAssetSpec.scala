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

import base.SpecBase
import models.WhatKindOfAsset._
import models.assets.{AssetMonetaryAmount, Assets, NonEeaBusinessType, OtherAssetType}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.{Lang, MessagesImpl}
import play.api.libs.json.{JsError, JsString, Json}
import viewmodels._

import java.time.LocalDate

class WhatKindOfAssetSpec extends SpecBase with ScalaCheckPropertyChecks {

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

        WhatKindOfAsset.nonMaxedOutOptions(Assets()) mustBe List(
          RadioOption("whatKindOfAsset", Money.toString),
          RadioOption("whatKindOfAsset", PropertyOrLand.toString),
          RadioOption("whatKindOfAsset", Shares.toString),
          RadioOption("whatKindOfAsset", Business.toString),
          RadioOption("whatKindOfAsset", NonEeaBusiness.toString),
          RadioOption("whatKindOfAsset", Partnership.toString),
          RadioOption("whatKindOfAsset", Other.toString)
        )

      }

      "there is a 'Money' asset" in {

        val moneyAsset = AssetMonetaryAmount(4000L)
        val assets: Assets = Assets(monetary = List(moneyAsset))

        WhatKindOfAsset.nonMaxedOutOptions(assets) mustBe List(
          RadioOption("whatKindOfAsset", PropertyOrLand.toString),
          RadioOption("whatKindOfAsset", Shares.toString),
          RadioOption("whatKindOfAsset", Business.toString),
          RadioOption("whatKindOfAsset", NonEeaBusiness.toString),
          RadioOption("whatKindOfAsset", Partnership.toString),
          RadioOption("whatKindOfAsset", Other.toString)
        )
      }

      "there are a 10 Completed 'other' assets" in {
        val generator = for (_ <- 0 to 10) yield OtherAssetType("desc", 200L)
        val assets = Assets(other = generator.toList)

        WhatKindOfAsset.nonMaxedOutOptions(assets) mustBe List(
          RadioOption("whatKindOfAsset", Money.toString),
          RadioOption("whatKindOfAsset", PropertyOrLand.toString),
          RadioOption("whatKindOfAsset", Shares.toString),
          RadioOption("whatKindOfAsset", Business.toString),
          RadioOption("whatKindOfAsset", NonEeaBusiness.toString),
          RadioOption("whatKindOfAsset", Partnership.toString)
        )
      }

      "there are a 25 non-EEA business assets" in {
        val generator = for (i <- 0 to 25) yield NonEeaBusinessType(Some(s"$i"), "orgName", UkAddress("line1", "line2", None, None, "NE981ZZ"), "GB", LocalDate.now, None, provisional = false)
        val assets = Assets(nonEEABusiness = generator.toList)

        WhatKindOfAsset.nonMaxedOutOptions(assets) mustBe List(
          RadioOption("whatKindOfAsset", Money.toString),
          RadioOption("whatKindOfAsset", PropertyOrLand.toString),
          RadioOption("whatKindOfAsset", Shares.toString),
          RadioOption("whatKindOfAsset", Business.toString),
          RadioOption("whatKindOfAsset", Partnership.toString),
          RadioOption("whatKindOfAsset", Other.toString)
        )
    }
    }

    "display label in correct language" when {

      val asset = WhatKindOfAsset.NonEeaBusiness

      "English" in {

        val messages: MessagesImpl = MessagesImpl(Lang("en"), messagesApi)
        val result = asset.label(messages)

        result mustBe "Non-EEA Company"

      }

      "Welsh" in {

        val messages: MessagesImpl = MessagesImpl(Lang("cy"), messagesApi)
        val result = asset.label(messages)

        result mustBe "whatKindOfAsset.NonEeaBusiness" // TODO - update unit test when Welsh keys added
      }
    }
  }
}
