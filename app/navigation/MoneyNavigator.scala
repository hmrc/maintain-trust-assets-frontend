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
import controllers.asset.routes
import models.UserAnswers
import pages.Page
import pages.asset.money._
import play.api.mvc.Call
import uk.gov.hmrc.auth.core.AffinityGroup

import javax.inject.{Inject, Singleton}

@Singleton
class MoneyNavigator @Inject()(config: FrontendAppConfig) extends Navigator(config) {

  override protected def route(draftId: String): PartialFunction[Page, AffinityGroup => UserAnswers => Call] = {
    case AssetMoneyValuePage(index) => _ => ua => assetMoneyValueRoute(ua, index, draftId)
  }

  private def assetMoneyValueRoute(answers: UserAnswers, index: Int, draftId: String): Call = {
    val assets = answers.get(sections.Assets).getOrElse(List.empty)
    assets match  {
      case Nil => routes.WhatKindOfAssetController.onPageLoad(index, draftId)
      case _ => routes.AddAssetsController.onPageLoad(draftId)
    }
  }

}
