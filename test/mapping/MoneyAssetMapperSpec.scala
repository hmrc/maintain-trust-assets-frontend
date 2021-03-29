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
import models.Status.Completed
import models._
import org.scalatest.{MustMatchers, OptionValues}
import pages.AssetStatus
import pages.asset._
import pages.asset.money._

class MoneyAssetMapperSpec extends SpecBase with MustMatchers
  with OptionValues with Generators {

  val moneyAssetMapper: MoneyAssetMapper = injector.instanceOf[MoneyAssetMapper]

  private val assetValue: Long = 2000L

  "MoneyAssetMapper" must {

    "not be able to create a money asset when no value is in user answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage, WhatKindOfAsset.Money).success.value

      moneyAssetMapper.build(userAnswers) mustNot be(defined)
    }

    // TODO

//    "able to create a Monetary Asset" in {
//
//      val userAnswers =
//        emptyUserAnswers
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Money).success.value
//          .set(AssetMoneyValuePage, assetValue).success.value
//          .set(AssetStatus, Completed).success.value
//
//      moneyAssetMapper.build(userAnswers).value mustBe List(AssetMonetaryAmount(assetValue))
//    }
//
//    "able to create multiple Monetary Assets" in {
//
//      val userAnswers =
//        emptyUserAnswers
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Money).success.value
//          .set(AssetMoneyValuePage, assetValue).success.value
//          .set(AssetStatus, Completed).success.value
//          .set(WhatKindOfAssetPage, WhatKindOfAsset.Money).success.value
//          .set(AssetMoneyValuePage, assetValue).success.value
//          .set(AssetStatus, Completed).success.value
//
//      moneyAssetMapper.build(userAnswers).value.length mustBe 2
//    }
  }
}
