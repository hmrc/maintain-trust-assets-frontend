/*
 * Copyright 2022 HM Revenue & Customs
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

package mapping

import base.SpecBase
import generators.Generators
import models.WhatKindOfAsset.PropertyOrLand
import models._
import models.assets.PropertyLandType
import org.scalatest.{MustMatchers, OptionValues}
import pages.asset._
import pages.asset.property_or_land._

class PropertyOrLandMapperSpec extends SpecBase with MustMatchers
  with OptionValues with Generators {

  private val mapper: PropertyOrLandMapper = injector.instanceOf[PropertyOrLandMapper]

  private val assetTotalValue: Long = 1000L
  private val assetTrustValue: Long = 750L

  "propertyOrLandMapper" must {

    "not be able to create a property or land asset when missing values in user answers" in {

      val answers = emptyUserAnswers
        .set(WhatKindOfAssetPage, PropertyOrLand).success.value
        .set(PropertyOrLandAddressYesNoPage, true).success.value

      mapper(answers).isDefined mustBe false

    }

    "be able to create a property or land Asset" when {

      "uk address" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage, true).success.value
          .set(PropertyOrLandAddressUkYesNoPage, true).success.value
          .set(PropertyOrLandUKAddressPage, UkAddress("26", "Grangetown", Some("Tyne and Wear"), Some("Newcastle"), "Z99 2YY")).success.value
          .set(PropertyOrLandTotalValuePage, assetTotalValue).success.value
          .set(TrustOwnAllThePropertyOrLandPage, false).success.value
          .set(PropertyLandValueTrustPage, assetTrustValue).success.value

        val result = mapper(userAnswers).get

        result mustBe
          PropertyLandType(
            buildingLandName = None,
            address = Some(
              UkAddress(
                line1 = "26",
                line2 = "Grangetown",
                line3 = Some("Tyne and Wear"),
                line4 = Some("Newcastle"),
                postcode = "Z99 2YY"
              )),
            valueFull = assetTotalValue,
            valuePrevious = Some(assetTrustValue)
          )
      }

      "international address" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.PropertyOrLand).success.value
          .set(PropertyOrLandAddressYesNoPage, true).success.value
          .set(PropertyOrLandAddressUkYesNoPage, false).success.value
          .set(PropertyOrLandInternationalAddressPage, NonUkAddress("1", "Broadway", Some("New York"), "US")).success.value
          .set(PropertyOrLandTotalValuePage, assetTotalValue).success.value
          .set(TrustOwnAllThePropertyOrLandPage, false).success.value
          .set(PropertyLandValueTrustPage, assetTrustValue).success.value

        val result = mapper(userAnswers).get

        result mustBe
          PropertyLandType(
            buildingLandName = None,
            address = Some(
              NonUkAddress(
                line1 = "1",
                line2 = "Broadway",
                line3 = Some("New York"),
                country = "US"
              )),
            valueFull = assetTotalValue,
            valuePrevious = Some(assetTrustValue)
          )
      }
    }

    "be able to create a property or land Asset with a property description and owns full value" in {

      val userAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage, WhatKindOfAsset.PropertyOrLand).success.value
        .set(PropertyOrLandAddressYesNoPage, false).success.value
        .set(PropertyOrLandDescriptionPage, "Property Or Land").success.value
        .set(PropertyOrLandTotalValuePage, assetTotalValue).success.value
        .set(TrustOwnAllThePropertyOrLandPage, true).success.value

      val result = mapper(userAnswers).get

      result mustBe
        PropertyLandType(
          buildingLandName = Some("Property Or Land"),
          address = None,
          valueFull = assetTotalValue,
          valuePrevious = Some(assetTotalValue)
      )
    }
  }
}
