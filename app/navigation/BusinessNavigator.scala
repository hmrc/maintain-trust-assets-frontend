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
import models.UserAnswers
import pages.Page
import pages.asset.business._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

import javax.inject.{Inject, Singleton}

@Singleton
class BusinessNavigator @Inject()(config: FrontendAppConfig) extends Navigator(config) {

  override protected def route(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case BusinessNamePage(index) => _ => _ => controllers.asset.business.routes.BusinessDescriptionController.onPageLoad(index, draftId)
    case BusinessDescriptionPage(index) => _ => _ => controllers.asset.business.routes.BusinessAddressUkYesNoController.onPageLoad(index, draftId)
    case BusinessAddressUkYesNoPage(index) => _ => ua => addressUkYesNoRoute(ua, index, draftId)
    case BusinessUkAddressPage(index) => _ => _ => controllers.asset.business.routes.BusinessValueController.onPageLoad(index, draftId)
    case BusinessInternationalAddressPage(index) => _ => _ => controllers.asset.business.routes.BusinessValueController.onPageLoad(index, draftId)
    case BusinessValuePage(index) => _ => _ => controllers.asset.business.routes.BusinessAnswersController.onPageLoad(index, draftId)
  }

  private def addressUkYesNoRoute(userAnswers: UserAnswers, index : Int, draftId: String) : Call = {
    userAnswers.get(BusinessAddressUkYesNoPage(index)) match {
      case Some(true) =>
        controllers.asset.business.routes.BusinessUkAddressController.onPageLoad(index, draftId)
      case Some(false) =>
        controllers.asset.business.routes.BusinessInternationalAddressController.onPageLoad(index, draftId)
      case _=>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

}
