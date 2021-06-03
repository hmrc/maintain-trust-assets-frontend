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

package pages.asset

import models.WhatKindOfAsset._
import models.{UserAnswers, WhatKindOfAsset}
import pages.asset.business._
import pages.asset.money._
import pages.asset.partnership._
import pages.asset.property_or_land._
import pages.asset.shares._
import pages.{AssetStatus, QuestionPage}
import play.api.libs.json.JsPath
import sections.Assets

import scala.util.{Success, Try}

case object WhatKindOfAssetPage extends QuestionPage[WhatKindOfAsset] {

  override def path: JsPath = JsPath \ Assets \ toString

  override def toString: String = "whatKindOfAsset"

  override def cleanup(value: Option[WhatKindOfAsset], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(Money) =>
        doCleanup(userAnswers, WhatKindOfAsset.values.filterNot(_ == Money))
      case Some(PropertyOrLand) =>
        doCleanup(userAnswers, WhatKindOfAsset.values.filterNot(_ == PropertyOrLand))
      case Some(Shares) =>
        doCleanup(userAnswers, WhatKindOfAsset.values.filterNot(_ == Shares))
      case Some(Business) =>
        doCleanup(userAnswers, WhatKindOfAsset.values.filterNot(_ == Business))
      case Some(Partnership) =>
        doCleanup(userAnswers, WhatKindOfAsset.values.filterNot(_ == Partnership))
      case Some(Other) =>
        doCleanup(userAnswers, WhatKindOfAsset.values.filterNot(_ == Other))
      case Some(NonEeaBusiness) =>
        doCleanup(userAnswers, WhatKindOfAsset.values.filterNot(_ == NonEeaBusiness))
      case _ => super.cleanup(value, userAnswers)
    }
  }

  private def doCleanup(userAnswers: UserAnswers, cleanup: List[WhatKindOfAsset]): Try[UserAnswers] = {
    cleanup.foldLeft[Try[UserAnswers]](Success(userAnswers))((updatedAnswers, asset) => {
      updatedAnswers match {
        case Success(ua) =>
          asset match {
            case Money => removeMoney(ua)
            case PropertyOrLand => removePropertyOrLand(ua)
            case Shares => removeShare(ua)
            case Business => removeBusiness(ua)
            case Partnership => removePartnership(ua)
            case Other => removeOther(ua)
            case NonEeaBusiness => removeNonEeaBusiness(ua)
          }
        case _ =>
          updatedAnswers
      }
    })
  }

  private def removeMoney(userAnswers: UserAnswers) : Try[UserAnswers] = {
    userAnswers.remove(AssetMoneyValuePage)
      .flatMap(removeStatus)
  }

  private def removeShare(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(SharesInAPortfolioPage)
      .flatMap(_.remove(ShareCompanyNamePage))
      .flatMap(_.remove(SharesOnStockExchangePage))
      .flatMap(_.remove(ShareClassPage))
      .flatMap(_.remove(ShareQuantityInTrustPage))
      .flatMap(_.remove(ShareValueInTrustPage))
      .flatMap(_.remove(SharePortfolioNamePage))
      .flatMap(_.remove(SharePortfolioOnStockExchangePage))
      .flatMap(_.remove(SharePortfolioQuantityInTrustPage))
      .flatMap(_.remove(SharePortfolioValueInTrustPage))
      .flatMap(removeStatus)
  }

  private def removePropertyOrLand(userAnswers: UserAnswers) : Try[UserAnswers] = {
    userAnswers.remove(PropertyOrLandAddressYesNoPage)
      .flatMap(_.remove(PropertyOrLandAddressUkYesNoPage))
      .flatMap(_.remove(PropertyOrLandUKAddressPage))
      .flatMap(_.remove(PropertyOrLandInternationalAddressPage))
      .flatMap(_.remove(PropertyOrLandTotalValuePage))
      .flatMap(_.remove(TrustOwnAllThePropertyOrLandPage))
      .flatMap(_.remove(PropertyOrLandDescriptionPage))
      .flatMap(_.remove(PropertyLandValueTrustPage))
      .flatMap(removeStatus)
  }

  private def removeOther(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(pages.asset.other.OtherAssetDescriptionPage)
      .flatMap(_.remove(pages.asset.other.OtherAssetValuePage))
      .flatMap(removeStatus)
  }

  private def removePartnership(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(PartnershipDescriptionPage)
      .flatMap(_.remove(PartnershipStartDatePage))
      .flatMap(removeStatus)
  }

  private def removeBusiness(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(BusinessNamePage)
      .flatMap(_.remove(BusinessDescriptionPage))
      .flatMap(_.remove(BusinessAddressUkYesNoPage))
      .flatMap(_.remove(BusinessUkAddressPage))
      .flatMap(_.remove(BusinessInternationalAddressPage))
      .flatMap(_.remove(BusinessValuePage))
      .flatMap(removeStatus)
  }

  private def removeNonEeaBusiness(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(noneeabusiness.NamePage)
      .flatMap(_.remove(noneeabusiness.NonUkAddressPage))
      .flatMap(_.remove(noneeabusiness.GoverningCountryPage))
      .flatMap(_.remove(noneeabusiness.add.StartDatePage))
      .flatMap(removeStatus)
  }

  private def removeStatus(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(AssetStatus)
  }

}
