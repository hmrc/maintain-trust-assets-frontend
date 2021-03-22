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
import controllers.asset.noneeabusiness.routes._
import models.UserAnswers
import pages.Page
import pages.asset.noneeabusiness._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

import javax.inject.{Inject, Singleton}

@Singleton
class NonEeaBusinessNavigator @Inject()(config: FrontendAppConfig) extends Navigator(config) {

  override protected def route(): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case NamePage(index) => _ => _ => InternationalAddressController.onPageLoad(index)
    case InternationalAddressPage(index) => _ => _ => GoverningCountryController.onPageLoad(index)
    case GoverningCountryPage(index) => _ => _ => StartDateController.onPageLoad(index)
    case StartDatePage(index) => _ => _ => AnswersController.onPageLoad(index)
  }

}
