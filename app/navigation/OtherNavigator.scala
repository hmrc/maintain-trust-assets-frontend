/*
 * Copyright 2026 HM Revenue & Customs
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
import pages.asset.other.add.OtherAnswerPage
import pages.asset.other.amend.IndexPage
import play.api.mvc.Call

import javax.inject.Inject

class OtherNavigator @Inject() () extends Navigator() {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(userAnswers, mode)(page)(userAnswers)

  def routes(ua: UserAnswers, mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(ua, mode)

  def simpleNavigation(ua: UserAnswers, mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case OtherAssetDescriptionPage(index) => _ => OtherAssetValueController.onPageLoad(index, mode)
    case OtherAssetValuePage(index)       => _ => navigateToCheckAnswers(ua, mode, index)
    case OtherAnswerPage(index)           => _ => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
  }

  private def navigateToCheckAnswers(ua: UserAnswers, mode: Mode, index: Int): Call =
    if (mode == NormalMode) {
      controllers.asset.other.add.routes.OtherAnswerController.onPageLoad(index)
    } else {
      ua.get(IndexPage) match {
        case Some(indexPage: Int) =>
          controllers.asset.other.amend.routes.AnswersController.renderFromUserAnswers(indexPage)
        case None                 => controllers.routes.SessionExpiredController.onPageLoad
      }
    }

}
