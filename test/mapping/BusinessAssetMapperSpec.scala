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
import models.assets.BusinessAssetType
import models.{NonUkAddress, UkAddress, WhatKindOfAsset}
import org.scalatest.{MustMatchers, OptionValues}
import pages.asset.WhatKindOfAssetPage
import pages.asset.business._

class BusinessAssetMapperSpec extends SpecBase with MustMatchers
  with OptionValues with Generators {

  val mapper: BusinessAssetMapper = injector.instanceOf[BusinessAssetMapper]

  private val assetValue: Long = 123L

  "BusinessAssetMapper" must {

    "must not be able to create a business asset when no description or value in user answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Business).success.value

      mapper.build(userAnswers) mustNot be(defined)
    }

    "be able to create a Business Asset" when {

      "uk address" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Business).success.value
          .set(BusinessNamePage, "businessName").success.value
          .set(BusinessDescriptionPage, "businessDesc").success.value
          .set(BusinessAddressUkYesNoPage, true).success.value
          .set(BusinessUkAddressPage, UkAddress("26", "Grangetown", Some("Tyne and Wear"), Some("Newcastle"), "Z99 2YY")).success.value
          .set(BusinessValuePage, assetValue).success.value

        val result = mapper(userAnswers).get

        result mustBe
          BusinessAssetType(
            orgName = "businessName",
            businessDescription = "businessDesc",
            address = UkAddress(
                line1 = "26",
                line2 = "Grangetown",
                line3 = Some("Tyne and Wear"),
                line4 = Some("Newcastle"),
                postcode = "Z99 2YY"
              ),
            businessValue = assetValue
          )
      }

      "international address" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Business).success.value
          .set(BusinessNamePage, "businessName").success.value
          .set(BusinessDescriptionPage, "businessDesc").success.value
          .set(BusinessAddressUkYesNoPage, false).success.value
          .set(BusinessInternationalAddressPage, NonUkAddress("1", "Broadway", Some("New York"), "US")).success.value
          .set(BusinessValuePage, assetValue).success.value

        val result = mapper(userAnswers).get

        result mustBe
          BusinessAssetType(
            orgName = "businessName",
            businessDescription = "businessDesc",
            address = NonUkAddress(
                line1 = "1",
                line2 = "Broadway",
                line3 = Some("New York"),
                country = "US"
              ),
            businessValue = assetValue
          )
      }
    }
  }
}
