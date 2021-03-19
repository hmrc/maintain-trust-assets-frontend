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

package navigation

import config.FrontendAppConfig
import controllers.asset.property_or_land.routes._
import controllers.asset.routes._
import models.UserAnswers
import pages.Page
import pages.asset.property_or_land._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

import javax.inject.{Inject, Singleton}

@Singleton
class PropertyOrLandNavigator @Inject()(config: FrontendAppConfig) extends Navigator(config) {

  override protected def route(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case page @ PropertyOrLandAddressYesNoPage(index) => _ => ua => yesNoNav(
      ua = ua,
      fromPage = page,
      yesCall = PropertyOrLandAddressUkYesNoController.onPageLoad(index, draftId),
      noCall = PropertyOrLandDescriptionController.onPageLoad(index, draftId)
    )
    case page @ PropertyOrLandAddressUkYesNoPage(index) => _ => ua => yesNoNav(
      ua = ua,
      fromPage = page,
      yesCall = PropertyOrLandUKAddressController.onPageLoad(index, draftId),
      noCall = PropertyOrLandInternationalAddressController.onPageLoad(index, draftId)
    )
    case PropertyOrLandDescriptionPage(index) => _ => _ => PropertyOrLandTotalValueController.onPageLoad(index, draftId)
    case PropertyOrLandUKAddressPage(index) => _ => _ => PropertyOrLandTotalValueController.onPageLoad(index, draftId)
    case PropertyOrLandInternationalAddressPage(index) => _ => _ => PropertyOrLandTotalValueController.onPageLoad(index, draftId)
    case PropertyOrLandTotalValuePage(index) => _ => _ => TrustOwnAllThePropertyOrLandController.onPageLoad(index, draftId)
    case page @ TrustOwnAllThePropertyOrLandPage(index) => _ => ua => yesNoNav(
      ua = ua,
      fromPage = page,
      yesCall = PropertyOrLandAnswerController.onPageLoad(index, draftId),
      noCall = PropertyLandValueTrustController.onPageLoad(index, draftId)
    )
    case PropertyLandValueTrustPage(index) => _ => _ => PropertyOrLandAnswerController.onPageLoad(index, draftId)
    case PropertyOrLandAnswerPage => _ => _ => AddAssetsController.onPageLoad(draftId)
  }

}
