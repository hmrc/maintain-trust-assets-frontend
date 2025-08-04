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
    case PropertyOrLandDescriptionPage(index) => _ => PropertyOrLandTotalValueController.onPageLoad(index, mode)
    case PropertyOrLandUKAddressPage(index)  => _ => PropertyOrLandTotalValueController.onPageLoad(index, mode)
    case PropertyOrLandInternationalAddressPage(index)  => _ => PropertyOrLandTotalValueController.onPageLoad(index, mode)
    case PropertyOrLandTotalValuePage(index) => _ => TrustOwnAllThePropertyOrLandController.onPageLoad(index, mode)
    case PropertyLandValueTrustPage(index) => ua => navigateToCheckAnswers(ua, mode, index)
    case PropertyOrLandAnswerPage(index) => _ => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case PropertyOrLandAddressYesNoPage(index) => ua => yesNoNav(
      ua = ua,
      fromPage = PropertyOrLandAddressYesNoPage(index),
      yesCall = PropertyOrLandAddressUkYesNoController.onPageLoad(index, mode),
      noCall = PropertyOrLandDescriptionController.onPageLoad(index, mode)
    )
    case PropertyOrLandAddressUkYesNoPage(index) => ua => yesNoNav(
      ua = ua,
      fromPage = PropertyOrLandAddressUkYesNoPage(index),
      yesCall = PropertyOrLandUKAddressController.onPageLoad(index, mode),
      noCall = PropertyOrLandInternationalAddressController.onPageLoad(index, mode)
    )
    case TrustOwnAllThePropertyOrLandPage(index) => ua => yesNoNav(
      ua = ua,
      fromPage = TrustOwnAllThePropertyOrLandPage(index),
      yesCall = navigateToCheckAnswers(ua, mode, index),
      noCall = PropertyLandValueTrustController.onPageLoad(index, mode)
    )
  }

  private def navigateToCheckAnswers(ua: UserAnswers, mode: Mode, index: Int): Call = {
    if (mode == NormalMode) {
      AssetNavigator.routeToIndex(
        List.empty, // TODO: COME BACK TO
        controllers.asset.property_or_land.add.routes.PropertyOrLandAnswerController.onPageLoad,
        index = Some(index)
      )
    } else {
      ua.get(IndexPage) match {
        case Some(indexPage) => controllers.asset.property_or_land.amend.routes.PropertyOrLandAmendAnswersController.renderFromUserAnswers(indexPage)
        case None => controllers.routes.SessionExpiredController.onPageLoad
      }
    }
  }
}
