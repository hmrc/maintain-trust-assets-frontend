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

package utils

import controllers.asset._
import models.assets._
import play.api.i18n.Messages
import utils.CheckAnswersFormatters.currencyFormat
import viewmodels.{AddRow, AddToRows}

class AddAssetViewHelper {

  def rows(assets: Assets, isNonTaxable: Boolean)(implicit messages: Messages): AddToRows = {

    val rows = assets.nonEEABusiness.zipWithIndex.map(x => renderNonEEABusiness(x._1, x._2)) ++
      assets.monetary.zipWithIndex.map(x => renderMoney(x._1, x._2)) ++
      assets.propertyOrLand.zipWithIndex.map(x => renderPropertyOrLand(x._1, x._2)) ++
      assets.other.zipWithIndex.map(x => renderOther(x._1, x._2)) ++
      assets.business.zipWithIndex.map(x => renderBusiness(x._1, x._2)) ++
      assets.partnerShip.zipWithIndex.map(x => renderPartnership(x._1, x._2)) ++
      assets.shares.zipWithIndex.map(x => renderShares(x._1, x._2))

    AddToRows(
      rows.filterNot(x => x.typeLabel != nonEeaBusinessLabel && isNonTaxable)
    )
  }

  private def renderMoney(asset: AssetMonetaryAmount, index: Int)(implicit messages: Messages): AddRow = {
    AddRow(
      name = currencyFormat(asset.assetMonetaryAmount.toString),
      typeLabel = messages("entities.asset.monetary"),
      changeUrl = money.amend.routes.MoneyAmendAnswersController.extractAndRender(index).url,
      removeUrl = money.remove.routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }

  private def renderOther(asset: OtherAssetType, index: Int)(implicit messages: Messages): AddRow = {
    AddRow(
      name = asset.description,
      typeLabel = messages("entities.asset.other"),
      changeUrl = other.amend.routes.AnswersController.extractAndRender(index).url,
      removeUrl = other.remove.routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }

  private def renderPropertyOrLand(asset: PropertyLandType, index: Int)(implicit messages: Messages): AddRow = {
    AddRow(
      name = asset.name,
      typeLabel = messages("entities.asset.propertyOrLand"),
      changeUrl = property_or_land.amend.routes.PropertyOrLandAmendAnswersController.extractAndRender(index).url,
      removeUrl = property_or_land.remove.routes.PropertyOrLandRemoveAssetYesNoController.onPageLoad(index).url
    )
  }

  private def renderBusiness(asset: BusinessAssetType, index: Int)(implicit messages: Messages): AddRow = {
    AddRow(
      name = asset.orgName,
      typeLabel = messages("entities.asset.business"),
      changeUrl = business.amend.routes.BusinessAmendAnswersController.extractAndRender(index).url,
      removeUrl = business.remove.routes.RemoveBusinessAssetYesNoController.onPageLoad(index).url
    )
  }

  private def nonEeaBusinessLabel(implicit messages: Messages): String = messages("entities.asset.nonEeaBusiness")

  private def renderNonEEABusiness(asset: NonEeaBusinessType, index: Int)(implicit messages: Messages): AddRow = {
    AddRow(
      name = asset.orgName,
      typeLabel = nonEeaBusinessLabel,
      changeUrl = noneeabusiness.amend.routes.AnswersController.extractAndRender(index).url,
      removeUrl = noneeabusiness.remove.routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }

  private def renderPartnership(asset: PartnershipType, index: Int)(implicit messages: Messages): AddRow = {
    AddRow(
      name = asset.description,
      typeLabel = messages("entities.asset.partnership"),
      changeUrl = partnership.amend.routes.PartnershipAmendAnswersController.extractAndRender(index).url,
      removeUrl = partnership.remove.routes.RemovePartnershipAssetYesNoController.onPageLoad(index).url
    )
  }

  private def renderShares(asset: SharesType, index: Int)(implicit messages: Messages): AddRow = {
    AddRow(
      name = asset.orgName,
      typeLabel = messages("entities.asset.shares"),
      changeUrl = shares.amend.routes.ShareAmendAnswersController.extractAndRender(index).url,
      removeUrl = shares.remove.routes.RemoveShareAssetYesNoController.onPageLoad(index).url
    )
  }
}
