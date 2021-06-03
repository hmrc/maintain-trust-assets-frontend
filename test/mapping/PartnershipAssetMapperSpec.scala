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
import models.WhatKindOfAsset
import org.scalatest.{MustMatchers, OptionValues}
import pages.asset.WhatKindOfAssetPage

class PartnershipAssetMapperSpec extends SpecBase with MustMatchers
  with OptionValues with Generators {

  val partnershipAssetMapper: PartnershipAssetMapper = injector.instanceOf[PartnershipAssetMapper]

  "PartnershipAssetMapper" must {

    "must not be able to create a partnership asset when no description or start date in user answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Partnership).success.value

      partnershipAssetMapper(userAnswers) mustNot be(defined)
    }

    // TODO

//    "must able to create a Partnership Asset" in {
//
//      val userAnswers =
//        emptyUserAnswers
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Partnership).success.value
//          .set(PartnershipDescriptionPage, "Partnership Description").success.value
//          .set(PartnershipStartDatePage, LocalDate.now).success.value
//          .set(AssetStatus, Completed).success.value
//
//      partnershipAssetMapper.build(userAnswers).value mustBe List(PartnershipType("Partnership Description", LocalDate.now))
//    }

//    "must able to create multiple Partnership Assets" in {
//
//      val userAnswers =
//        emptyUserAnswers
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Partnership).success.value
//          .set(PartnershipDescriptionPage, "Partnership Description 1").success.value
//          .set(PartnershipStartDatePage, LocalDate.now).success.value
//          .set(AssetStatus, Completed).success.value
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Partnership).success.value
//          .set(PartnershipDescriptionPage, "Partnership Description 2").success.value
//          .set(PartnershipStartDatePage, LocalDate.now).success.value
//          .set(AssetStatus, Completed).success.value
//
//      partnershipAssetMapper.build(userAnswers).value mustBe List(
//        PartnershipType("Partnership Description 1", LocalDate.now),
//        PartnershipType("Partnership Description 2", LocalDate.now)
//      )
//
//      partnershipAssetMapper.build(userAnswers).value.length mustBe 2
//    }
  }
}
