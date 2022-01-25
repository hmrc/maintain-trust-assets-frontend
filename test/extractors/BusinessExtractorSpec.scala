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
import models.assets.BusinessAssetType
import models.{NonUkAddress, UkAddress, UserAnswers}
import pages.asset.business._
import pages.asset.business.amend.IndexPage

class BusinessExtractorSpec extends SpecBase {

  private val index = 0
  private val name: String = "BusinessName"
  private val description: String = "BusinessDescription"
  private val valueFull: Long = 790L
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "FR")
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "postCode")

  private val extractor = new BusinessExtractor()

  "BusinessExtractor" must {

    "Populate user answers" when {

      val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = false)

      "has a none uk address asset data" in {

        val businessAsset = BusinessAssetType(
          orgName = name,
          businessDescription = description,
          address = nonUkAddress,
          businessValue = valueFull
        )

        val result = extractor(baseAnswers, businessAsset, index).get

        result.get(IndexPage).get mustBe index
        result.get(BusinessNamePage) mustBe Some(name)
        result.get(BusinessDescriptionPage) mustBe Some(description)
        result.get(BusinessAddressUkYesNoPage) mustBe Some(false)
        result.get(BusinessInternationalAddressPage) mustBe Some(nonUkAddress)
        result.get(BusinessValuePage) mustBe Some(valueFull)
      }

      "has a uk address asset data" in {

        val businessAsset = BusinessAssetType(
          orgName = name,
          businessDescription = description,
          address = ukAddress,
          businessValue = valueFull
        )

        val result = extractor(baseAnswers, businessAsset, index).get

        result.get(IndexPage).get mustBe index
        result.get(BusinessNamePage) mustBe Some(name)
        result.get(BusinessDescriptionPage) mustBe Some(description)
        result.get(BusinessAddressUkYesNoPage) mustBe Some(true)
        result.get(BusinessUkAddressPage) mustBe Some(ukAddress)
        result.get(BusinessValuePage) mustBe Some(valueFull)
      }
    }
  }
}
