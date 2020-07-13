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

import controllers.actions.RedirectToPageInJourney
import models.Mode
import models.UserAnswers
import models.Status.Completed
import play.api.i18n.Messages
import sections.Assets
import viewmodels._
import viewmodels.{AddRow, AddToRows}
import controllers.asset._
import navigation.AddAssetViewHelperNavigator

class AddAssetViewHelper(userAnswers: UserAnswers, mode: Mode, draftId: String)(implicit messages: Messages) {

  private val redirectToPageInJourney: RedirectToPageInJourney = new RedirectToPageInJourney(userAnswers, draftId)

  private val navigationPages: AddAssetViewHelperNavigator = new AddAssetViewHelperNavigator

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
    AddRow(
      name = mvm.value,
      typeLabel = mvm.`type`.toString,
      changeUrl = redirectToPageInJourney.redirect(
        money.routes.AssetMoneyValueController.onPageLoad(mode, index, draftId),
        navigationPages.money(index)
      ).url,
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
      changeUrl = redirectToPageInJourney.redirect(
        property_or_land.routes.PropertyOrLandAnswerController.onPageLoad(index, draftId),
        navigationPages.propertyOrLand(index)
      ).url,
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

  private def parseShare(svm: ShareAssetViewModel, index : Int) : AddRow = {
    val defaultName = messages("entities.no.name.added")

    AddRow(
      name = svm.name.getOrElse(defaultName),
      typeLabel = svm.`type`.toString,
      changeUrl = redirectToPageInJourney.redirect(
        shares.routes.ShareAnswerController.onPageLoad(index, draftId),
        navigationPages.shares(index)
      ).url,
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

  private def parseBusiness(bvm: BusinessAssetViewModel, index: Int) : AddRow = {
    AddRow(
      name = bvm.name,
      typeLabel = bvm.`type`.toString,
      changeUrl = redirectToPageInJourney.redirect(
        business.routes.BusinessAnswersController.onPageLoad(index, draftId),
        navigationPages.business(index)
      ).url,
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

  private def parsePartnership(pvm: PartnershipAssetViewModel, index: Int) : AddRow = {
    AddRow(
      name = pvm.description,
      typeLabel = pvm.`type`.toString,
      changeUrl = redirectToPageInJourney.redirect(
        partnership.routes.PartnershipAnswerController.onPageLoad(index, draftId),
        navigationPages.partnership(index)
      ).url,
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

  private def parseOther(ovm: OtherAssetViewModel, index: Int) : AddRow = {
    AddRow(
      name = ovm.description,
      typeLabel = ovm.`type`.toString,
      changeUrl = redirectToPageInJourney.redirect(
        other.routes.OtherAssetAnswersController.onPageLoad(index, draftId),
        navigationPages.other(index)
      ).url,
      removeUrl = routes.RemoveAssetYesNoController.onPageLoad(index, draftId).url
    )
  }

}
