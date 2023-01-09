/*
 * Copyright 2023 HM Revenue & Customs
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
import models.UserAnswers
import models.assets.OtherAssetType
import pages.asset.other.amend.IndexPage
import pages.asset.other.{OtherAssetDescriptionPage, OtherAssetValuePage}

class OtherExtractorSpec extends SpecBase {

  private val index = 0

  private val extractor = new OtherAssetExtractor()

  "OtherExtractor" must {

    "Populate user answers" when {

      val baseAnswers: UserAnswers = emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true, isUnderlyingData5mld = false)

      "has asset data" in {

        val otherAsset = OtherAssetType(
          description = "Other Asset",
          value = 4000
        )

        val result = extractor(baseAnswers, otherAsset, index).get

        result.get(IndexPage) mustBe Some(index)
        result.get(OtherAssetDescriptionPage) mustBe Some("Other Asset")
        result.get(OtherAssetValuePage) mustBe Some(4000)
      }
    }
  }
}
