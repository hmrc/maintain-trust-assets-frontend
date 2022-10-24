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

package extractors

import base.SpecBase
import models.assets.PropertyLandType
import models.{NonUkAddress, UkAddress, UserAnswers}
import pages.asset.property_or_land._
import pages.asset.property_or_land.amend.IndexPage

class PropertyOrLandExtractorSpec extends SpecBase {

  private val index = 0
  private val name: String = "BusinessName"
  private val valueFull: Long = 790L
  private val valuePrevious: Long = 380L
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "FR")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postCode")

  private val extractor = new PropertyOrLandExtractor()

  "PropertyOrLandExtractor" must {

    "Populate user answers" when {

      val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = false)

      "has no address asset data and Trust Owns does not own all the land" in {

        val propertyOrLandAsset = PropertyLandType(
          buildingLandName = Some(name),
          address = None,
          valueFull = valueFull,
          valuePrevious = Some(valuePrevious)
        )

        val result = extractor(baseAnswers, propertyOrLandAsset, index).get

        result.get(IndexPage).get mustBe index
        result.get(PropertyOrLandAddressYesNoPage).get mustBe false
        result.get(PropertyOrLandDescriptionPage).get mustBe name
        result.get(PropertyOrLandAddressUkYesNoPage).isDefined mustBe false
        result.get(PropertyOrLandUKAddressPage).isDefined mustBe false
        result.get(PropertyOrLandInternationalAddressPage).isDefined mustBe false
        result.get(PropertyOrLandTotalValuePage).get mustBe valueFull
        result.get(TrustOwnAllThePropertyOrLandPage).get mustBe false
        result.get(PropertyLandValueTrustPage).get mustBe valuePrevious
      }

      "has no address asset data and Trust Owns all the land - previous populated" in {

        val propertyOrLandAsset = PropertyLandType(
          buildingLandName = Some(name),
          address = None,
          valueFull = valueFull,
          valuePrevious = Some(valueFull)
        )

        val result = extractor(baseAnswers, propertyOrLandAsset, index).get

        result.get(IndexPage).get mustBe index
        result.get(PropertyOrLandAddressYesNoPage).get mustBe false
        result.get(PropertyOrLandDescriptionPage).get mustBe name
        result.get(PropertyOrLandAddressUkYesNoPage).isDefined mustBe false
        result.get(PropertyOrLandUKAddressPage).isDefined mustBe false
        result.get(PropertyOrLandInternationalAddressPage).isDefined mustBe false
        result.get(PropertyOrLandTotalValuePage).get mustBe valueFull
        result.get(TrustOwnAllThePropertyOrLandPage).get mustBe true
        result.get(PropertyLandValueTrustPage).isDefined mustBe false
      }

      "has no address asset data and Trust Owns all the land - previous not populated" in {

        val propertyOrLandAsset = PropertyLandType(
          buildingLandName = Some(name),
          address = None,
          valueFull = valueFull,
          valuePrevious = None
        )

        val result = extractor(baseAnswers, propertyOrLandAsset, index).get

        result.get(IndexPage).get mustBe index
        result.get(PropertyOrLandAddressYesNoPage).get mustBe false
        result.get(PropertyOrLandDescriptionPage).get mustBe name
        result.get(PropertyOrLandAddressUkYesNoPage).isDefined mustBe false
        result.get(PropertyOrLandUKAddressPage).isDefined mustBe false
        result.get(PropertyOrLandInternationalAddressPage).isDefined mustBe false
        result.get(PropertyOrLandTotalValuePage).get mustBe valueFull
        result.get(TrustOwnAllThePropertyOrLandPage).get mustBe true
        result.get(PropertyLandValueTrustPage).isDefined mustBe false
      }

      "has a none uk address asset data and Trust Owns all the land" in {

        val propertyOrLandAsset = PropertyLandType(
          buildingLandName = None,
          address = Some(nonUkAddress),
          valueFull = valueFull,
          valuePrevious = None
        )

        val result = extractor(baseAnswers, propertyOrLandAsset, index).get

        result.get(IndexPage).get mustBe index
        result.get(PropertyOrLandAddressYesNoPage).get mustBe true
        result.get(PropertyOrLandDescriptionPage).isDefined mustBe false
        result.get(PropertyOrLandAddressUkYesNoPage).get mustBe false
        result.get(PropertyOrLandUKAddressPage).isDefined mustBe false
        result.get(PropertyOrLandInternationalAddressPage).get mustBe nonUkAddress
        result.get(PropertyOrLandTotalValuePage).get mustBe valueFull
        result.get(TrustOwnAllThePropertyOrLandPage).get mustBe true
        result.get(PropertyLandValueTrustPage).isDefined mustBe false
      }

      "has a uk address asset data and Trust Owns all the land" in {

        val propertyOrLandAsset = PropertyLandType(
          buildingLandName = None,
          address = Some(ukAddress),
          valueFull = valueFull,
          valuePrevious = None
        )

        val result = extractor(baseAnswers, propertyOrLandAsset, index).get

        result.get(IndexPage).get mustBe index
        result.get(PropertyOrLandAddressYesNoPage).get mustBe true
        result.get(PropertyOrLandDescriptionPage).isDefined mustBe false
        result.get(PropertyOrLandAddressUkYesNoPage).get mustBe true
        result.get(PropertyOrLandUKAddressPage).get mustBe ukAddress
        result.get(PropertyOrLandInternationalAddressPage).isDefined mustBe false
        result.get(PropertyOrLandTotalValuePage).get mustBe valueFull
        result.get(TrustOwnAllThePropertyOrLandPage).get mustBe true
        result.get(PropertyLandValueTrustPage).isDefined mustBe false
      }
    }

  }

}
