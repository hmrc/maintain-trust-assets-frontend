/*
 * Copyright 2024 HM Revenue & Customs
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
import pages.asset.WhatKindOfAssetPage.key
import pages.asset.business._
import pages.asset.money._
import pages.asset.partnership._
import pages.asset.property_or_land._
import pages.asset.shares._
import pages.{AssetStatus, QuestionPage}
import play.api.libs.json.JsPath
import sections.Assets

import scala.util.{Success, Try}

final case class WhatKindOfAssetPage(index: Int) extends QuestionPage[WhatKindOfAsset] {

  override def path: JsPath = JsPath \ Assets \ toString

  override def toString: String = key

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
    userAnswers.remove(AssetMoneyValuePage(index))
      .flatMap(removeStatus)
  }

  private def removeShare(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(SharesInAPortfolioPage(index))
      .flatMap(_.remove(ShareCompanyNamePage(index)))
      .flatMap(_.remove(SharesOnStockExchangePage(index)))
      .flatMap(_.remove(ShareClassPage(index)))
      .flatMap(_.remove(ShareQuantityInTrustPage(index)))
      .flatMap(_.remove(ShareValueInTrustPage(index)))
      .flatMap(_.remove(SharePortfolioNamePage(index)))
      .flatMap(_.remove(SharePortfolioOnStockExchangePage(index)))
      .flatMap(_.remove(SharePortfolioQuantityInTrustPage(index)))
      .flatMap(_.remove(SharePortfolioValueInTrustPage(index)))
      .flatMap(removeStatus)
  }

  private def removePropertyOrLand(userAnswers: UserAnswers) : Try[UserAnswers] = {
    userAnswers.remove(PropertyOrLandAddressYesNoPage(index))
      .flatMap(_.remove(PropertyOrLandAddressUkYesNoPage(index)))
      .flatMap(_.remove(PropertyOrLandUKAddressPage(index)))
      .flatMap(_.remove(PropertyOrLandInternationalAddressPage(index)))
      .flatMap(_.remove(PropertyOrLandTotalValuePage(index)))
      .flatMap(_.remove(TrustOwnAllThePropertyOrLandPage(index)))
      .flatMap(_.remove(PropertyOrLandDescriptionPage(index)))
      .flatMap(_.remove(PropertyLandValueTrustPage(index)))
      .flatMap(removeStatus)
  }

  private def removeOther(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(pages.asset.other.OtherAssetDescriptionPage(index))
      .flatMap(_.remove(pages.asset.other.OtherAssetValuePage(index)))
      .flatMap(removeStatus)
  }

  // TODO: COME BACK TO...
  private def removePartnership(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(PartnershipDescriptionPage(index))
      .flatMap(_.remove(PartnershipStartDatePage(index)))
      .flatMap(removeStatus)
  }

  private def removeBusiness(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(BusinessNamePage(0))
      .flatMap(_.remove(BusinessDescriptionPage(0)))
      .flatMap(_.remove(BusinessAddressUkYesNoPage(0)))
      .flatMap(_.remove(BusinessUkAddressPage(0)))
      .flatMap(_.remove(BusinessInternationalAddressPage(0)))
      .flatMap(_.remove(BusinessValuePage(0)))
      .flatMap(removeStatus)
  }

  private def removeNonEeaBusiness(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(noneeabusiness.NamePage(index))
      .flatMap(_.remove(noneeabusiness.NonUkAddressPage(index)))
      .flatMap(_.remove(noneeabusiness.GoverningCountryPage(index)))
      .flatMap(_.remove(noneeabusiness.add.StartDatePage))
      .flatMap(removeStatus)
  }

  private def removeStatus(userAnswers: UserAnswers): Try[UserAnswers] = {
    userAnswers.remove(AssetStatus(index))
  }
}

object WhatKindOfAssetPage {
  val key: String = "whatKindOfAsset"
}