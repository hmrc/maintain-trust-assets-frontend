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

package navigation

import base.SpecBase
import generators.Generators
import models.{NormalMode, UserAnswers}
import models.WhatKindOfAsset.Money
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.WhatKindOfAssetPage
import pages.asset.money.AssetMoneyValuePage

class MoneyNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[MoneyNavigator]

  "Money Navigator" must {

    "go to AddAssetsPage from AssetMoneyValue page when the amount submitted" in {

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>

          val answers = userAnswers.set(WhatKindOfAssetPage, Money).success.value

          navigator.nextPage(AssetMoneyValuePage, NormalMode, answers)
            .mustBe(controllers.asset.routes.AddAssetsController.onPageLoad())

      }
    }
  }

}
