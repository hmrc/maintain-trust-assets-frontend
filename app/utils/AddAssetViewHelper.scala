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
import models.Status.Completed
import models.{NormalMode, UserAnswers}
import models.WhatKindOfAsset.Money.AssetLabel
import play.api.i18n.Messages
import sections.Assets
import viewmodels.{AddRow, AddToRows, _}
import javax.inject.Inject

class AddAssetViewHelper @Inject()(userAnswers: UserAnswers)
                                  (implicit messages: Messages) {

  def rows: AddToRows = {

    val assets = userAnswers.get(Assets).toList.flatten.zipWithIndex

    val completed: List[AddRow] = assets.filter(_._1.status == Completed).flatMap(parseAsset)

    val inProgress: List[AddRow] = assets.filterNot(_._1.status == Completed).flatMap(parseAsset)

    AddToRows(inProgress, completed)
  }

  private val defaultValue = messages("entities.no.value.added")
  private val defaultAddress = messages("entities.no.address.added")
  private val defaultDescription = messages("entities.no.description.added")
  private val defaultName = messages("entities.no.name.added")

  private def parseAsset(asset: (AssetViewModel, Int)): Option[AddRow] = {
    val vm = asset._1
    val index = asset._2

    vm match {
      case money: MoneyAssetViewModel => Some(parseMoney(money, index))
      case share: ShareAssetViewModel => Some(parseShare(share, index))
      case propertyOrLand: PropertyOrLandAssetViewModel => Some(parsePropertyOrLand(propertyOrLand, index))
      case business: BusinessAssetViewModel => Some(parseBusiness(business, index))
      case partnership: PartnershipAssetViewModel => Some(parsePartnership(partnership, index))
      case other: OtherAssetViewModel => Some(parseOther(other, index))
      case nonEeaBusiness: NonEeaBusinessAssetViewModel => Some(parseNonEeaBusiness(nonEeaBusiness, index))
      case _ => None
    }
  }

  private def parseMoney(mvm: MoneyAssetViewModel, index: Int): AddRow = {
    AddRow(
      name = mvm.label.getOrElse(defaultValue),
      typeLabel = mvm.`type`.label,
      changeUrl = money.routes.AssetMoneyValueController.onPageLoad(NormalMode).url,
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }

  private def parsePropertyOrLand(plvm: PropertyOrLandAssetViewModel, index: Int): AddRow = {
    AddRow(
      name = (plvm.hasAddress, plvm.address, plvm.description) match {
        case (Some(true), address, _) => address.getOrElse(defaultAddress)
        case (Some(false), _, description) => description.getOrElse(defaultDescription)
        case _ => messages("entities.no.addressOrDescription.added")
      },
      typeLabel = plvm.`type`.label,
      changeUrl = if (plvm.status == Completed) {
        property_or_land.routes.PropertyOrLandAnswerController.onPageLoad().url
      } else {
        property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }

  private def parseShare(svm: ShareAssetViewModel, index: Int): AddRow = {
    AddRow(
      name = svm.label.getOrElse(defaultName),
      typeLabel = svm.`type`.label,
      changeUrl = if (svm.status == Completed) {
        shares.routes.ShareAnswerController.onPageLoad().url
      } else {
        shares.routes.SharesInAPortfolioController.onPageLoad(NormalMode).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }

  private def parseBusiness(bvm: BusinessAssetViewModel, index: Int): AddRow = {
    AddRow(
      name = bvm.label.getOrElse(defaultName),
      typeLabel = bvm.`type`.label,
      changeUrl = if (bvm.status == Completed) {
        business.routes.BusinessAnswersController.onPageLoad().url
      } else {
        business.routes.BusinessNameController.onPageLoad(NormalMode).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }

  private def parsePartnership(pvm: PartnershipAssetViewModel, index: Int): AddRow = {
    AddRow(
      name = pvm.label.getOrElse(defaultDescription),
      typeLabel = pvm.`type`.label,
      changeUrl = if (pvm.status == Completed) {
        partnership.routes.PartnershipAnswerController.onPageLoad().url
      } else {
        partnership.routes.PartnershipDescriptionController.onPageLoad(NormalMode).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }

  private def parseOther(ovm: OtherAssetViewModel, index: Int): AddRow = {
    AddRow(
      name = ovm.label.getOrElse(defaultDescription),
      typeLabel = ovm.`type`.label,
      changeUrl = if (ovm.status == Completed) {
        other.routes.OtherAssetAnswersController.onPageLoad().url
      } else {
        other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }
  
  private def parseNonEeaBusiness(nebvm: NonEeaBusinessAssetViewModel, index: Int): AddRow = {
    AddRow(
      name = nebvm.label.getOrElse(defaultName),
      typeLabel = nebvm.`type`.label,
      changeUrl = if (nebvm.status == Completed) {
        noneeabusiness.routes.AnswersController.onPageLoad().url
      } else {
        noneeabusiness.routes.NameController.onPageLoad(NormalMode).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index).url
    )
  }

}
