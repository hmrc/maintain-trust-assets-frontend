/*
 * Copyright 2025 HM Revenue & Customs
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

package extensions

import base.SpecBase
import extensions.Enhancers._
import models.WhatKindOfAsset
import models.WhatKindOfAsset.prefix

class EnhancersSpec extends SpecBase {

  "Enhancers" when {

    "StringEnhancer" when {

      "uncapitalise" must {

        "uncapitalise first letter of each asset type" when {

          "Money" in {
            val messageKey = WhatKindOfAsset.Money.toString
            val string = messages(s"$prefix.$messageKey")
            string.uncapitalise mustEqual "money"
          }

          "Property or land" in {
            val messageKey = WhatKindOfAsset.PropertyOrLand.toString
            val string = messages(s"$prefix.$messageKey")
            string.uncapitalise mustEqual "property or land"
          }

          "Shares" in {
            val messageKey = WhatKindOfAsset.Shares.toString
            val string = messages(s"$prefix.$messageKey")
            string.uncapitalise mustEqual "shares"
          }

          "Business" in {
            val messageKey = WhatKindOfAsset.Business.toString
            val string = messages(s"$prefix.$messageKey")
            string.uncapitalise mustEqual "business"
          }

          "Partnership" in {
            val messageKey = WhatKindOfAsset.Partnership.toString
            val string = messages(s"$prefix.$messageKey")
            string.uncapitalise mustEqual "partnership"
          }

          "Other" in {
            val messageKey = WhatKindOfAsset.Other.toString
            val string = messages(s"$prefix.$messageKey")
            string.uncapitalise mustEqual "other"
          }

        }

        "uncapitalise the first word" when {
          "Non-EEA Company" in {
            val messageKey = WhatKindOfAsset.NonEeaBusiness.toString
            val string = messages(s"$prefix.$messageKey")
            string.lowercaseFirstLetterOfFirstWord mustEqual "company outside the UK or EEA"
          }

          "Empty string" in {
            "".lowercaseFirstLetterOfFirstWord mustEqual ""
          }
        }
      }
    }
  }

}
