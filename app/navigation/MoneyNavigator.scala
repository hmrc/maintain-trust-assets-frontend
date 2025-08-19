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

import models.{Mode, NormalMode, UserAnswers}
import pages.Page
import pages.asset.money._
import pages.asset.money.add.MoneyAnswerPage
import play.api.mvc.Call

import javax.inject.Inject

class MoneyNavigator @Inject()() extends Navigator {

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    routes(mode)(page)(userAnswers)

  def routes(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case AssetMoneyValuePage(index) => ua => navigateToCheckAnswers(ua, mode, index)
    case MoneyAnswerPage(index) => _ => controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
  }

  private def navigateToCheckAnswers(ua: UserAnswers, mode: Mode, index: Int): Call = {
    if (mode == NormalMode) {
      AssetNavigator.routeToIndex(
        List.empty,
        controllers.asset.money.add.routes.MoneyAnswerController.onPageLoad,
        index = Some(index)
      )
    } else {
      ua.get(AssetMoneyValuePage(index)) match {
        case Some(moneyVlue) => controllers.asset.money.add.routes.MoneyAnswerController.onPageLoad(index)
        case None => controllers.routes.SessionExpiredController.onPageLoad
      }
    }
  }
}
