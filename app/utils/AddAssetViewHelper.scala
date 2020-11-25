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

package utils

import models.{Mode, UserAnswers}
import models.Status.Completed
import play.api.i18n.Messages
import sections.Assets
import viewmodels._
import viewmodels.{AddRow, AddToRows}
import controllers.asset._
import utils.CheckAnswersFormatters.currencyFormat

class AddAssetViewHelper(userAnswers: UserAnswers, mode: Mode, draftId: String)(implicit messages: Messages) {

  def rows: AddToRows = {

    val assets = userAnswers.get(Assets).toList.flatten.zipWithIndex

    val completed: List[AddRow] = assets.filter(_._1.status == Completed).flatMap(parseAsset)

    val inProgress: List[AddRow] = assets.filterNot(_._1.status == Completed).flatMap(parseAsset)

    AddToRows(inProgress, completed)
  }

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
      case _ => None
    }
  }

  private def parseMoney(mvm: MoneyAssetViewModel, index: Int) : AddRow = {
    val defaultValue = messages("entities.no.value.added")

    AddRow(
      name = mvm.value match {
        case Some(value) => currencyFormat(value)
        case None => defaultValue
      },
      typeLabel = mvm.`type`.toString,
      changeUrl = money.routes.AssetMoneyValueController.onPageLoad(mode, index, draftId).url,
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

  private def parsePropertyOrLand(plvm : PropertyOrLandAssetViewModel, index: Int) : AddRow = {
    val defaultAddressName = messages("entities.no.address.added")
    val defaultDescriptionName = messages("entities.no.description.added")

    val typeLabel : String = messages("addAssets.propertyOrLand")

    AddRow(
      name = plvm match {
        case PropertyOrLandAssetUKAddressViewModel(_, address, _) => address.getOrElse(defaultAddressName)
        case PropertyOrLandAssetInternationalAddressViewModel(_, address, _) => address.getOrElse(defaultAddressName)
        case PropertyOrLandAssetAddressViewModel(_, address, _) => address.getOrElse(defaultAddressName)
        case PropertyOrLandAssetDescriptionViewModel(_, description, _) => description.getOrElse(defaultDescriptionName)
        case PropertyOrLandDefaultViewModel(_, _) => messages("entities.no.addressOrDescription.added")
      },
      typeLabel = typeLabel,
      changeUrl = if (plvm.status == Completed) {
        property_or_land.routes.PropertyOrLandAnswerController.onPageLoad(index, draftId).url
      } else {
        property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(mode, index, draftId).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

  private def parseShare(svm: ShareAssetViewModel, index : Int) : AddRow = {
    val defaultName = messages("entities.no.name.added")

    AddRow(
      name = svm.name.getOrElse(defaultName),
      typeLabel = svm.`type`.toString,
      changeUrl = if (svm.status == Completed) {
        shares.routes.ShareAnswerController.onPageLoad(index, draftId).url
      } else {
        shares.routes.SharesInAPortfolioController.onPageLoad(mode, index, draftId).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

  private def parseBusiness(bvm: BusinessAssetViewModel, index: Int) : AddRow = {
    AddRow(
      name = bvm.name,
      typeLabel = bvm.`type`.toString,
      changeUrl = if (bvm.status == Completed) {
        business.routes.BusinessAnswersController.onPageLoad(index, draftId).url
      } else {
        business.routes.BusinessNameController.onPageLoad(mode, index, draftId).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

  private def parsePartnership(pvm: PartnershipAssetViewModel, index: Int) : AddRow = {
    AddRow(
      name = pvm.description,
      typeLabel = pvm.`type`.toString,
      changeUrl = if (pvm.status == Completed) {
        partnership.routes.PartnershipAnswerController.onPageLoad(index, draftId).url
      } else {
        partnership.routes.PartnershipDescriptionController.onPageLoad(mode, index, draftId).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

  private def parseOther(ovm: OtherAssetViewModel, index: Int) : AddRow = {
    AddRow(
      name = ovm.description,
      typeLabel = ovm.`type`.toString,
      changeUrl = if (ovm.status == Completed) {
        other.routes.OtherAssetAnswersController.onPageLoad(index, draftId).url
      } else {
        other.routes.OtherAssetDescriptionController.onPageLoad(mode, index, draftId).url
      },
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

}
