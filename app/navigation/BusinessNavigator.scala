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

import controllers.asset.business.routes._
import models.assets.Assets
import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.business._
import play.api.mvc.Call

import javax.inject.Inject

class BusinessNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  override def nextPage(page: Page, userAnswers: UserAnswers, assets: Assets = Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil)): Call =
    nextPage(page, NormalMode, userAnswers)

  def simpleNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case BusinessNamePage => _ => BusinessDescriptionController.onPageLoad(mode)
    case BusinessDescriptionPage => _ => BusinessAddressUkYesNoController.onPageLoad(mode)
    case BusinessUkAddressPage => _ => BusinessValueController.onPageLoad(mode)
    case BusinessInternationalAddressPage => _ => BusinessValueController.onPageLoad(mode)
    case BusinessValuePage => _ => BusinessAnswersController.onPageLoad()
  }

  private def yesNoNavigation(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case BusinessAddressUkYesNoPage => ua => yesNoNav(
      ua = ua,
      fromPage = BusinessAddressUkYesNoPage,
      yesCall = BusinessUkAddressController.onPageLoad(mode),
      noCall = BusinessInternationalAddressController.onPageLoad(mode)
    )
  }

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] =
  simpleNavigation(mode) orElse
    yesNoNavigation(mode)

}
