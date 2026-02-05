/*
 * Copyright 2025 HM Revenue & Customs
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

import controllers.asset.noneeabusiness.add.{routes => addRts}
import controllers.asset.noneeabusiness.amend.{routes => amendRts}
import controllers.asset.noneeabusiness.{routes => rts}
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.noneeabusiness._
import pages.asset.noneeabusiness.add.StartDatePage
import pages.asset.noneeabusiness.amend.IndexPage
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class NonEeaBusinessNavigator @Inject() () extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
    simpleNavigation(mode)

  def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case NamePage(index)             => _ => rts.InternationalAddressController.onPageLoad(index, mode)
    case NonUkAddressPage(index)     => _ => rts.GoverningCountryController.onPageLoad(index, mode)
    case GoverningCountryPage(index) => ua => navigateToStartDateOrCheckAnswers(ua, mode, index)
    case StartDatePage(index)        => _ => addRts.AnswersController.onPageLoad(index)
  }

  private def navigateToStartDateOrCheckAnswers(ua: UserAnswers, mode: Mode, index: Int): Call =
    if (mode == NormalMode) {
      addRts.StartDateController.onPageLoad(index)
    } else {
      ua.get(IndexPage) match {
        case Some(indexPage: Int) => amendRts.AnswersController.renderFromUserAnswers(indexPage)
        case None                 => controllers.routes.SessionExpiredController.onPageLoad
      }
    }

}
