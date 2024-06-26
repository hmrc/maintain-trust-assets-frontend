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

package navigation

import controllers.asset.property_or_land.routes._
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.property_or_land._
import pages.asset.property_or_land.add.PropertyOrLandAnswerPage
import pages.asset.property_or_land.amend.IndexPage
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class PropertyOrLandNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode) orElse
      yesNoNavigation(mode)

  def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case PropertyOrLandDescriptionPage => _ => PropertyOrLandTotalValueController.onPageLoad(mode)
    case PropertyOrLandUKAddressPage  => _ => PropertyOrLandTotalValueController.onPageLoad(mode)
    case PropertyOrLandInternationalAddressPage  => _ => PropertyOrLandTotalValueController.onPageLoad(mode)
    case PropertyOrLandTotalValuePage => _ => TrustOwnAllThePropertyOrLandController.onPageLoad(mode)
    case PropertyLandValueTrustPage => ua => navigateToCheckAnswers(ua, mode)
    case PropertyOrLandAnswerPage => _ => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case PropertyOrLandAddressYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = PropertyOrLandAddressYesNoPage,
      yesCall = PropertyOrLandAddressUkYesNoController.onPageLoad(mode),
      noCall = PropertyOrLandDescriptionController.onPageLoad(mode)
    )
    case PropertyOrLandAddressUkYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = PropertyOrLandAddressUkYesNoPage,
      yesCall = PropertyOrLandUKAddressController.onPageLoad(mode),
      noCall = PropertyOrLandInternationalAddressController.onPageLoad(mode)
    )
    case TrustOwnAllThePropertyOrLandPage => ua => yesNoNav(
      ua = ua,
      fromPage = TrustOwnAllThePropertyOrLandPage,
      yesCall = navigateToCheckAnswers(ua, mode),
      noCall = PropertyLandValueTrustController.onPageLoad(mode)
    )
  }

  private def navigateToCheckAnswers(ua: UserAnswers, mode: Mode): Call = {
    if (mode == NormalMode) {
      controllers.asset.property_or_land.add.routes.PropertyOrLandAnswerController.onPageLoad()
    } else {
      ua.get(IndexPage) match {
        case Some(index) => controllers.asset.property_or_land.amend.routes.PropertyOrLandAmendAnswersController.renderFromUserAnswers(index)
        case None => controllers.routes.SessionExpiredController.onPageLoad
      }
    }
  }
}
