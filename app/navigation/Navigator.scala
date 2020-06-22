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
import javax.inject.{Inject, Singleton}
import play.api.mvc.Call
import controllers.routes
import pages._
import models._
import uk.gov.hmrc.auth.core.AffinityGroup

@Singleton
class Navigator @Inject()(config: FrontendAppConfig) {

  private def defaultRoute: PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case _ => _ => _ => routes.IndexController.onPageLoad()
  }

  protected def route(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] =
//      AssetsRoutes.route(draftId) orElse
      defaultRoute

  def nextPage(page: Page, mode: Mode, draftId: String, af :AffinityGroup = AffinityGroup.Organisation): UserAnswers => Call = mode match {
    case NormalMode =>
      route(draftId)(page)(af)
    case CheckMode =>
      route(draftId)(page)(af)
  }
}
