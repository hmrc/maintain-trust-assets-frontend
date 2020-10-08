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

package navigation

import config.FrontendAppConfig
import controllers.asset.property_or_land.routes
import javax.inject.{Inject, Singleton}
import models.{NormalMode, UserAnswers}
import pages.Page
import pages.asset.property_or_land._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

@Singleton
class PropertyOrLandNavigator @Inject()(config: FrontendAppConfig) extends Navigator(config) {

  override protected def route(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case PropertyOrLandAddressYesNoPage(index) => _ => propertyOrLandAddressYesNoPage(draftId, index)
    case PropertyOrLandAddressUkYesNoPage(index) => _ => propertyOrLandAddressUkYesNoPage(draftId, index)
    case PropertyOrLandDescriptionPage(index) => _ => _ => routes.PropertyOrLandTotalValueController.onPageLoad(NormalMode, index, draftId)
    case PropertyOrLandUKAddressPage(index) => _ => _ => routes.PropertyOrLandTotalValueController.onPageLoad(NormalMode, index, draftId)
    case PropertyOrLandInternationalAddressPage(index) => _ => _ => routes.PropertyOrLandTotalValueController.onPageLoad(NormalMode, index, draftId)
    case PropertyOrLandTotalValuePage(index) => _ => _ => routes.TrustOwnAllThePropertyOrLandController.onPageLoad(NormalMode, index, draftId)
    case TrustOwnAllThePropertyOrLandPage(index) => _ => trustOwnAllThePropertyOrLandPage(draftId, index)
    case PropertyLandValueTrustPage(index) => _ => _ => routes.PropertyOrLandAnswerController.onPageLoad(index, draftId)
    case PropertyOrLandAnswerPage => _ => _ => controllers.asset.routes.AddAssetsController.onPageLoad(draftId)
  }

  private def propertyOrLandAddressYesNoPage(draftId: String, index: Int)(answers: UserAnswers) = answers.get(PropertyOrLandAddressYesNoPage(index)) match {
    case Some(true)  => routes.PropertyOrLandAddressUkYesNoController.onPageLoad(NormalMode, index, draftId)
    case Some(false) => routes.PropertyOrLandDescriptionController.onPageLoad(NormalMode, index, draftId)
    case None        => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def propertyOrLandAddressUkYesNoPage(draftId: String, index: Int)(answers: UserAnswers) = answers.get(PropertyOrLandAddressUkYesNoPage(index)) match {
    case Some(true)  => routes.PropertyOrLandUKAddressController.onPageLoad(NormalMode, index, draftId)
    case Some(false) => routes.PropertyOrLandInternationalAddressController.onPageLoad(NormalMode, index, draftId)
    case None        => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def trustOwnAllThePropertyOrLandPage(draftId: String, index: Int)(answers: UserAnswers) = answers.get(TrustOwnAllThePropertyOrLandPage(index)) match {
    case Some(true) => routes.PropertyOrLandAnswerController.onPageLoad(index, draftId)
    case Some(false)  => routes.PropertyLandValueTrustController.onPageLoad(NormalMode, index, draftId)
    case None        => controllers.routes.SessionExpiredController.onPageLoad()
  }

}
