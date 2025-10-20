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

package extractors

import base.SpecBase
import models.UserAnswers
import models.assets.PartnershipType
import pages.asset.partnership._
import pages.asset.partnership.amend.IndexPage

import java.time.LocalDate

class PartnershipExtractorSpec extends SpecBase {

    private val description: String = "PartnershipDescription"
  private val startDate: LocalDate = LocalDate.now()

  private val extractor = new PartnershipAssetExtractor()

  "PartnershipExtractor" must {

    "Populate user answers" in {

      val baseAnswers: UserAnswers = emptyUserAnswers.copy(
        is5mldEnabled = true,
        isTaxable = true,
        isUnderlyingData5mld = false
      )

      val partnershipAsset = PartnershipType(
        description = description,
        partnershipStart = startDate
      )

      val result = extractor(baseAnswers, partnershipAsset, index).get

      result.get(IndexPage).get mustBe index
      result.get(PartnershipDescriptionPage(index)) mustBe Some(description)
      result.get(PartnershipStartDatePage(index)) mustBe Some(startDate)

    }
  }
}
