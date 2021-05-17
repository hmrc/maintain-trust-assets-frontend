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

package utils

import base.SpecBase
import controllers.asset._
import models.{Mode, NonUkAddress}
import viewmodels.AddRow
import java.time.LocalDate

import models.Status.Completed
import models.WhatKindOfAsset.Money
import models.assets.{AssetMonetaryAmount, Assets, NonEeaBusinessType}
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.money.AssetMoneyValuePage

class AddAssetViewHelperSpec extends SpecBase {

  def removeAssetYesNoRoute(index: Int): String =
    "/foo"

  "AddAssetViewHelper" when {

    ".row" must {

      "generate Nil for no user answers" in {
        val rows = new AddAssetViewHelper(Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil)).rows
        rows.inProgress mustBe Nil
        rows.complete mustBe Nil
      }

      "generate rows from user answers for complete assets" in {
        val nonEeaAsset = NonEeaBusinessType(None, "Non-EEA Business Name", NonUkAddress("", "", None, ""), "", LocalDate.now, None, true)
        val moneyAsset = AssetMonetaryAmount(4000)

        val assets = Assets(List(moneyAsset), Nil, Nil, Nil, Nil, Nil, List(nonEeaAsset))

        def changeNonEeaBusinessAssetRoute(index: Int): String =
          noneeabusiness.amend.routes.AnswersController.extractAndRender(index).url

        def removeNonEeaBusinessAssetRoute(index: Int): String =
          noneeabusiness.remove.routes.RemoveAssetYesNoController.onPageLoad(index).url

        val rows = new AddAssetViewHelper(assets).rows
        rows.complete mustBe List(
          AddRow("Non-EEA Business Name", typeLabel = "Non-EEA Company", changeNonEeaBusinessAssetRoute(0), removeNonEeaBusinessAssetRoute(0)),
          AddRow("4000", typeLabel = "Money", removeAssetYesNoRoute(1), removeAssetYesNoRoute(1))
        )
        rows.inProgress mustBe Nil
      }

    }
  }
}
