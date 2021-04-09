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

package mapping

import base.SpecBase
import generators.Generators
import models._
import org.scalatest.{MustMatchers, OptionValues}
import pages.asset._
import pages.asset.property_or_land._

class PropertyOrLandMapperSpec extends SpecBase with MustMatchers
  with OptionValues with Generators {

  val propertyOrLandMapper: PropertyOrLandMapper = injector.instanceOf[PropertyOrLandMapper]

  //private val assetTotalValue: Long = 1000L
  //private val assetTrustValue: Long = 750L

  "propertyOrLandMapper" must {

    "not be able to create a property or land asset when missing values in user answers" in {

      val userAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage, WhatKindOfAsset.PropertyOrLand).success.value
        .set(PropertyOrLandAddressYesNoPage, true).success.value

      propertyOrLandMapper.build(userAnswers) mustNot be(defined)

    }

    // TODO

//    "be able to create a property or land Asset" when {
//
//      "uk address" in {
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.PropertyOrLand).success.value
//          .set(PropertyOrLandAddressYesNoPage, true).success.value
//          .set(PropertyOrLandAddressUkYesNoPage, true).success.value
//          .set(PropertyOrLandUKAddressPage, UKAddress("26", "Grangetown", Some("Tyne and Wear"), Some("Newcastle"), "Z99 2YY")).success.value
//          .set(PropertyOrLandTotalValuePage, assetTotalValue).success.value
//          .set(TrustOwnAllThePropertyOrLandPage, false).success.value
//          .set(PropertyLandValueTrustPage, assetTrustValue).success.value
//
//        propertyOrLandMapper.build(userAnswers).value mustBe
//          List(
//            PropertyLandType(
//              buildingLandName = None,
//              address = Some(
//                AddressType(
//                  line1 = "26",
//                  line2 = "Grangetown",
//                  line3 = Some("Tyne and Wear"),
//                  line4 = Some("Newcastle"),
//                  postCode = Some("Z99 2YY"),
//                  country = "GB"
//                )),
//              valueFull = assetTotalValue,
//              valuePrevious = assetTrustValue
//
//            )
//          )
//      }
//
//      "international address" in {
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.PropertyOrLand).success.value
//          .set(PropertyOrLandAddressYesNoPage, true).success.value
//          .set(PropertyOrLandAddressUkYesNoPage, false).success.value
//          .set(PropertyOrLandInternationalAddressPage, InternationalAddress("1", "Broadway", Some("New York"), "US")).success.value
//          .set(PropertyOrLandTotalValuePage, assetTotalValue).success.value
//          .set(TrustOwnAllThePropertyOrLandPage, false).success.value
//          .set(PropertyLandValueTrustPage, assetTrustValue).success.value
//
//        propertyOrLandMapper.build(userAnswers).value mustBe
//          List(
//            PropertyLandType(
//              buildingLandName = None,
//              address = Some(
//                AddressType(
//                  line1 = "1",
//                  line2 = "Broadway",
//                  line3 = Some("New York"),
//                  line4 = None,
//                  postCode = None,
//                  country = "US"
//                )),
//              valueFull = assetTotalValue,
//              valuePrevious = assetTrustValue
//
//            )
//          )
//      }
//    }

//    "be able to create a property or land Asset with a property description and owns full value" in {
//
//      val userAnswers = emptyUserAnswers
//        .set(WhatKindOfAssetPage, WhatKindOfAsset.PropertyOrLand).success.value
//        .set(PropertyOrLandAddressYesNoPage, false).success.value
//        .set(PropertyOrLandDescriptionPage, "Property Or Land").success.value
//        .set(PropertyOrLandTotalValuePage, assetTotalValue).success.value
//        .set(TrustOwnAllThePropertyOrLandPage, true).success.value
//
//
//      propertyOrLandMapper.build(userAnswers).value mustBe
//        List(
//          PropertyLandType(
//            buildingLandName = Some("Property Or Land"),
//            address = None,
//            valueFull = assetTotalValue,
//            valuePrevious = assetTotalValue
//
//          )
//        )
//    }

//    "be able to create multiple Share Assets" in {
//      val userAnswers = emptyUserAnswers
//
//        .set(WhatKindOfAssetPage, WhatKindOfAsset.PropertyOrLand).success.value
//        .set(PropertyOrLandAddressYesNoPage, true).success.value
//        .set(PropertyOrLandAddressUkYesNoPage, true).success.value
//        .set(PropertyOrLandUKAddressPage, UKAddress("26", "Grangetown", Some("Tyne and Wear"), Some("Newcastle"), "Z99 2YY")).success.value
//        .set(PropertyOrLandTotalValuePage, assetTotalValue).success.value
//        .set(TrustOwnAllThePropertyOrLandPage, true).success.value
//        .set(PropertyLandValueTrustPage, assetTrustValue).success.value
//        .set(WhatKindOfAssetPage, WhatKindOfAsset.PropertyOrLand).success.value
//        .set(PropertyOrLandAddressYesNoPage, true).success.value
//        .set(PropertyOrLandAddressUkYesNoPage, true).success.value
//        .set(PropertyOrLandUKAddressPage, UKAddress("26", "Grangetown", Some("Tyne and Wear"), Some("Newcastle"), "Z99 2YY")).success.value
//        .set(PropertyOrLandTotalValuePage, assetTotalValue).success.value
//        .set(TrustOwnAllThePropertyOrLandPage, true).success.value
//        .set(PropertyLandValueTrustPage, assetTrustValue).success.value
//
//      propertyOrLandMapper.build(userAnswers).value.length mustBe 2
//    }
  }

}
