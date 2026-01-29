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
import models.WhatKindOfAsset.Money
import models.assets.AssetMonetaryAmount
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import pages.asset.WhatKindOfAssetPage
import pages.asset.money.AssetMoneyValuePage

class MoneyAssetMapperSpec extends SpecBase with Matchers with OptionValues with Generators {

  val moneyAssetMapper: MoneyAssetMapper = injector.instanceOf[MoneyAssetMapper]

  private val assetValue1: Long = 4000L

  "MoneyAssetMapper" must {

    "must not be able to create an other asset when no description or value in user answers" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage(index), Money)
          .success
          .value

      moneyAssetMapper(userAnswers).isDefined mustBe false
    }

    "must able to create an Other Asset" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage(index), Money)
          .success
          .value
          .set(AssetMoneyValuePage(index), assetValue1)
          .success
          .value

      val result = moneyAssetMapper(userAnswers).get

      result mustBe
        AssetMonetaryAmount(assetValue1)
    }
  }

}
