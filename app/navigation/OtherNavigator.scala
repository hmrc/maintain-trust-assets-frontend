/*
 * Copyright 2022 HM Revenue & Customs
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

import controllers.asset.other.routes._
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.other._
import pages.asset.other.amend.IndexPage
import play.api.mvc.Call

import javax.inject.Inject

class OtherNavigator @Inject()() extends Navigator() {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(userAnswers, mode)(page)(userAnswers)

  def routes(ua: UserAnswers, mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(ua, mode)

  def simpleNavigation(ua: UserAnswers, mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case OtherAssetDescriptionPage => _ => OtherAssetValueController.onPageLoad(mode)
    case OtherAssetValuePage => _ => navigateToCheckAnswers(ua, mode)
  }

  private def navigateToCheckAnswers(ua: UserAnswers, mode: Mode): Call = {
    if (mode == NormalMode) {
      controllers.asset.other.add.routes.OtherAnswerController.onPageLoad()
    } else {
      ua.get(IndexPage) match {
        case Some(index) => controllers.asset.other.amend.routes.AnswersController.renderFromUserAnswers(index)
        case None => controllers.routes.SessionExpiredController.onPageLoad()
      }
    }
  }
}
