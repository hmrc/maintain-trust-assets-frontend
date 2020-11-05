/*
 * Copyright 2020 HM Revenue & Customs
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
import models.Status.Completed
import models.{OtherAssetType, WhatKindOfAsset}
import org.scalatest.{MustMatchers, OptionValues}
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.other._

class OtherAssetMapperSpec extends SpecBase with MustMatchers
  with OptionValues with Generators {

  val otherAssetMapper: Mapping[List[OtherAssetType]] = injector.instanceOf[OtherAssetMapper]

  private val assetValue1: Long = 4000L
  private val assetValue2: Long = 6000L

  "OtherAssetMapper" must {

    "must not be able to create an other asset when no description or value in user answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage(0), WhatKindOfAsset.Other).success.value

      otherAssetMapper.build(userAnswers) mustNot be(defined)
    }

    "must able to create an Other Asset" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage(0), WhatKindOfAsset.Other).success.value
          .set(OtherAssetDescriptionPage(0), "Description").success.value
          .set(OtherAssetValuePage(0), assetValue1).success.value
          .set(AssetStatus(0), Completed).success.value

      otherAssetMapper.build(userAnswers).value mustBe List(OtherAssetType("Description", assetValue1))
    }

    "must able to create multiple Other Assets" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage(0), WhatKindOfAsset.Other).success.value
          .set(OtherAssetDescriptionPage(0), "Description 1").success.value
          .set(OtherAssetValuePage(0), assetValue1).success.value
          .set(AssetStatus(0), Completed).success.value
          .set(WhatKindOfAssetPage(1), WhatKindOfAsset.Other).success.value
          .set(OtherAssetDescriptionPage(1), "Description 2").success.value
          .set(OtherAssetValuePage(1), assetValue2).success.value
          .set(AssetStatus(1), Completed).success.value

      otherAssetMapper.build(userAnswers).value mustBe List(
        OtherAssetType("Description 1", assetValue1),
        OtherAssetType("Description 2", assetValue2)
      )

      otherAssetMapper.build(userAnswers).value.length mustBe 2
    }
  }
}
