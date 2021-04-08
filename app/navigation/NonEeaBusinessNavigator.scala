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

import controllers.asset.noneeabusiness.{routes => rts}
import controllers.asset.noneeabusiness.add.{routes => addRts}
import controllers.asset.noneeabusiness.amend.{routes => amendRts}
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.noneeabusiness._
import play.api.mvc.Call
import javax.inject.{Inject, Singleton}

@Singleton
class NonEeaBusinessNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  override def nextPage(page: Page, userAnswers: UserAnswers): Call =
    nextPage(page, NormalMode, userAnswers)

  def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case NamePage => _ => rts.InternationalAddressController.onPageLoad(mode)
    case NonUkAddressPage => _ => rts.GoverningCountryController.onPageLoad(mode)
    case GoverningCountryPage => navigateToStartDateOrCheckAnswers(_, mode)
    case StartDatePage => _ => addRts.AnswersController.onPageLoad()
  }

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode)

  private def navigateToStartDateOrCheckAnswers(ua: UserAnswers, mode: Mode): Call = {
    if (mode == NormalMode) {
      rts.StartDateController.onPageLoad(mode)
    } else {
      ua.get(IndexPage) match {
        case Some(index) => amendRts.AnswersController.renderFromUserAnswers(index)
        case None => controllers.routes.SessionExpiredController.onPageLoad()
      }
    }
  }

}
