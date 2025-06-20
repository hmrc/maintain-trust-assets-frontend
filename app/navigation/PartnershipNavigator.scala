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

import controllers.asset.partnership.routes._
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.partnership._
import pages.asset.partnership.add.PartnershipAnswerPage
import pages.asset.partnership.amend.IndexPage
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class PartnershipNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case PartnershipDescriptionPage(index)  => _ => PartnershipStartDateController.onPageLoad(index, mode)
    case PartnershipStartDatePage(index)           => ua => navigateToCheckAnswers(ua, mode, index)
    case PartnershipAnswerPage(index)           => _ => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoadWithIndex(index)
  }

  private def navigateToCheckAnswers(ua: UserAnswers, mode: Mode, index: Int): Call = {
    if (mode == NormalMode) {
      AssetNavigator.routeToIndex(
        List.empty, // TODO: COME BACK TO
        controllers.asset.partnership.add.routes.PartnershipAnswerController.onPageLoad,
        index = Some(index)
      )
    } else {
      ua.get(IndexPage) match {
        case Some(index) => controllers.asset.partnership.amend.routes.PartnershipAmendAnswersController.renderFromUserAnswers(index)
        case None => controllers.routes.SessionExpiredController.onPageLoad
      }
    }
  }
}