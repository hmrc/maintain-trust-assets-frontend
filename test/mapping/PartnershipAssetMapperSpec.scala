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

package mapping

import base.SpecBase
import generators.Generators
import models.WhatKindOfAsset
import models.assets.PartnershipType
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import pages.asset.WhatKindOfAssetPage
import pages.asset.partnership._

import java.time.LocalDate

class PartnershipAssetMapperSpec extends SpecBase with Matchers with OptionValues with Generators {

  val partnershipAssetMapper: PartnershipAssetMapper = injector.instanceOf[PartnershipAssetMapper]

  "PartnershipAssetMapper" must {

    "must not be able to create a partnership asset when no description or start date in user answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage(index), WhatKindOfAsset.Partnership)
          .success
          .value

      partnershipAssetMapper(userAnswers) mustNot be(defined)
    }

    "must able to create a Partnership Asset" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage(index), WhatKindOfAsset.Partnership)
          .success
          .value
          .set(PartnershipDescriptionPage(index), "Partnership Description")
          .success
          .value
          .set(PartnershipStartDatePage(index), LocalDate.now)
          .success
          .value

      partnershipAssetMapper(userAnswers).value mustBe PartnershipType("Partnership Description", LocalDate.now)

    }

  }

}
