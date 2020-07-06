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

package controllers.actions.property_or_land

import com.google.inject.Inject
import models.WhatKindOfAsset.PropertyOrLand
import models.{Mode, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._
import play.api.mvc.Call

class PostActionRequirements @Inject()(userAnswers: UserAnswers,
                                       mode: Mode,
                                       index: Int,
                                       draftId: String) {

  def apply(): Either[Call, Unit] = {
    requireWhatKindOfAsset()
  }

  private def requireWhatKindOfAsset(): Either[Call, Unit] = {
    userAnswers.get(WhatKindOfAssetPage(index)) match {
      case Some(PropertyOrLand) =>
        requireAddressYesNo()
      case _ =>
        Left(controllers.asset.routes.WhatKindOfAssetController.onPageLoad(mode, index, draftId))
    }
  }

  private def requireAddressYesNo(): Either[Call, Unit] = {
    userAnswers.get(PropertyOrLandAddressYesNoPage(index)) match {
      case Some(true) =>
        requireAddressUkYesNo()
      case Some(false) =>
        requireDescription()
      case None =>
        Left(controllers.asset.property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(mode, index, draftId))
    }
  }

  private def requireAddressUkYesNo(): Either[Call, Unit] = {
    userAnswers.get(PropertyOrLandAddressUkYesNoPage(index)) match {
      case Some(true) =>
        requireUkAddress()
      case Some(false) =>
        requireInternationalAddress()
      case None =>
        Left(controllers.asset.property_or_land.routes.PropertyOrLandAddressUkYesNoController.onPageLoad(mode, index, draftId))
    }
  }

  private def requireUkAddress(): Either[Call, Unit] = {
    userAnswers.get(PropertyOrLandUKAddressPage(index)) match {
      case Some(_) =>
        requireTotalValue()
      case None =>
        Left(controllers.asset.property_or_land.routes.PropertyOrLandUKAddressController.onPageLoad(mode, index, draftId))
    }
  }

  private def requireInternationalAddress(): Either[Call, Unit] = {
    userAnswers.get(PropertyOrLandInternationalAddressPage(index)) match {
      case Some(_) =>
        requireTotalValue()
      case None =>
        Left(controllers.asset.property_or_land.routes.PropertyOrLandInternationalAddressController.onPageLoad(mode, index, draftId))
    }
  }

  private def requireDescription(): Either[Call, Unit] = {
    userAnswers.get(PropertyOrLandDescriptionPage(index)) match {
      case Some(_) =>
        requireTotalValue()
      case None =>
        Left(controllers.asset.property_or_land.routes.PropertyOrLandDescriptionController.onPageLoad(mode, index, draftId))
    }
  }

  private def requireTotalValue(): Either[Call, Unit] = {
    userAnswers.get(PropertyOrLandTotalValuePage(index)) match {
      case Some(_) =>
        requireDoesTrustOwnAllPropertyOrLand()
      case None =>
        Left(controllers.asset.property_or_land.routes.PropertyOrLandTotalValueController.onPageLoad(mode, index, draftId))
    }
  }

  private def requireDoesTrustOwnAllPropertyOrLand(): Either[Call, Unit] = {
    userAnswers.get(TrustOwnAllThePropertyOrLandPage(index)) match {
      case Some(true) =>
        Right(())
      case Some(false) =>
        requireValueOwnedByTrust()
      case None =>
        Left(controllers.asset.property_or_land.routes.TrustOwnAllThePropertyOrLandController.onPageLoad(mode, index, draftId))
    }
  }

  private def requireValueOwnedByTrust(): Either[Call, Unit] = {
    userAnswers.get(PropertyLandValueTrustPage(index)) match {
      case Some(_) =>
        Right(())
      case None =>
        Left(controllers.asset.property_or_land.routes.PropertyLandValueTrustController.onPageLoad(mode, index, draftId))
    }
  }
}
