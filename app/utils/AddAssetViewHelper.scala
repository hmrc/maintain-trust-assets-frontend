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

import controllers.asset._
import javax.inject.Inject
import models.Status.Completed
import models.{Mode, NormalMode, UserAnswers}
import play.api.i18n.Messages
import sections.Assets
import viewmodels._

class AddAssetViewHelper @Inject()(userAnswers: UserAnswers)
                                  (implicit messages: Messages) {

  def rows: AddToRows = {

    val assets = userAnswers.get(Assets).toList.flatten.zipWithIndex

    val complete = assets.filter(_._1.status == Completed).flatMap(parseAsset)

    AddToRows(Nil, complete)
  }

  private val defaultName = messages("entities.no.name.added")

  private def parseAsset(asset: (AssetViewModel, Int)): Option[AddRow] = {
    val vm = asset._1
    val index = asset._2

    vm match {
      case money: MoneyAssetViewModel => Some(renderMoney(money, mode = NormalMode))
      case nonEeaBusiness: NonEeaBusinessAssetViewModel => Some(renderNonEEABusiness(nonEeaBusiness, index))
      case _ => None
    }
  }

  private def renderMoney(asset: MoneyAssetViewModel, mode: Mode): AddRow = {
    AddRow(
      name = asset.label.getOrElse(defaultName),
      typeLabel = messages(s"entities.asset.money"),
      changeUrl = money.routes.AssetMoneyValueController.onPageLoad(mode).url,
      removeUrl = ???
    )
  }

  private def renderNonEEABusiness(asset: NonEeaBusinessAssetViewModel, index: Int): AddRow = {
    AddRow(
      name = asset.label.getOrElse(defaultName),
      typeLabel = messages(s"entities.asset.nonEeaBusiness"),
      changeUrl = noneeabusiness.amend.routes.AnswersController.extractAndRender(index).url,
      removeUrl = noneeabusiness.remove.routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }
}
