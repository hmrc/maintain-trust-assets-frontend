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
import models.NormalMode
import play.api.i18n.Messages
import viewmodels.{AddRow, AddToRows}
import javax.inject.Inject
import models.assets.{AssetMonetaryAmount, Assets, BusinessAssetType, NonEeaBusinessType, OtherAssetType, PartnershipType, PropertyLandType, SharesType}

class AddAssetViewHelper @Inject()(assets: Assets)
                                  (implicit messages: Messages) {

  def rows: AddToRows = {

    val complete = assets.nonEEABusiness.zipWithIndex.map(x => renderNonEEABusiness(x._1, x._2))
//        assets.business.zipWithIndex.map(x => renderBusiness(x._1, x._2)) ++
//        assets.monetary.zipWithIndex.map(x => renderMonetary(x._1, x._2)) ++
//        assets.partnerShip.zipWithIndex.map(x => renderPartnership(x._1, x._2)) ++
//        assets.propertyOrLand.zipWithIndex.map(x => renderPropertyOrLand(x._1, x._2)) ++
//        assets.shares.zipWithIndex.map(x => renderShares(x._1, x._2)) ++
//        assets.other.zipWithIndex.map(x => renderOther(x._1, x._2))

    AddToRows(Nil, complete)
  }


  private def renderMonetary(asset: AssetMonetaryAmount, index: Int): AddRow = {
    AddRow(
      name = asset.assetMonetaryAmount.toString,
      typeLabel = messages(s"entities.asset.monetary"),
      changeUrl = "", //money.routes.AssetMoneyValueController.onPageLoad(NormalMode).url,
      removeUrl = ""
    )
  }

  private def renderPropertyOrLand(asset: PropertyLandType, index: Int): AddRow = {
    AddRow(
      name = asset.valueFull.toString,
      typeLabel = messages(s"entities.asset.propertyOrLand"),
      changeUrl = "", //property_or_land.routes.PropertyOrLandAnswerController.onPageLoad().url,
      removeUrl = ""
    )
  }

  private def renderShares(asset: SharesType, index: Int): AddRow = {
    AddRow(
      name = asset.orgName,
      typeLabel = messages(s"entities.asset.shares"),
      changeUrl = "", //shares.routes.ShareAnswerController.onPageLoad().url,
      removeUrl = ""
    )
  }

  private def renderBusiness(asset: BusinessAssetType, index: Int): AddRow = {
    AddRow(
      name = asset.orgName,
      typeLabel = messages(s"entities.asset.business"),
      changeUrl = "", //business.routes.BusinessNameController.onPageLoad(NormalMode).url,
      removeUrl = ""
    )
  }

  private def renderPartnership(asset: PartnershipType, index: Int): AddRow = {
    AddRow(
      name = asset.description,
      typeLabel = messages(s"entities.asset.partnership"),
      changeUrl = "", //partnership.routes.PartnershipAnswerController.onPageLoad().url,
      removeUrl = ""
    )
  }

  private def renderOther(asset: OtherAssetType, index: Int): AddRow = {
    AddRow(
      name = asset.description,
      typeLabel = messages(s"entities.asset.other"),
      changeUrl = "", //other.routes.OtherAssetAnswersController.onPageLoad().url,
      removeUrl = ""
    )
  }
  
  private def renderNonEEABusiness(asset: NonEeaBusinessType, index: Int): AddRow = {
    AddRow(
      name = asset.orgName,
      typeLabel = messages(s"entities.asset.nonEeaBusiness"),
      changeUrl = noneeabusiness.routes.NameController.onPageLoad(NormalMode).url,
      removeUrl = ""
    )
  }

}
