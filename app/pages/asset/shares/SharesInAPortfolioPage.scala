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

package pages.asset.shares

import models.UserAnswers
import pages.{AssetStatus, QuestionPage}
import play.api.libs.json.JsPath

import scala.util.Try

case object SharesInAPortfolioPage extends QuestionPage[Boolean] {

  override def path: JsPath = basePath \ toString

  override def toString: String = "sharesInPortfolioYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(true) =>

        userAnswers.remove(SharesOnStockExchangePage)
          .flatMap(_.remove(ShareCompanyNamePage))
          .flatMap(_.remove(ShareClassPage))
          .flatMap(_.remove(ShareQuantityInTrustPage))
          .flatMap(_.remove(ShareValueInTrustPage))
          .flatMap(_.remove(AssetStatus))

      case Some(false) =>

        userAnswers.remove(SharePortfolioNamePage)
          .flatMap(_.remove(SharePortfolioOnStockExchangePage))
          .flatMap(_.remove(SharePortfolioQuantityInTrustPage))
          .flatMap(_.remove(SharePortfolioValueInTrustPage))
          .flatMap(_.remove(AssetStatus))

      case _ => super.cleanup(value, userAnswers)
    }
  }
}
