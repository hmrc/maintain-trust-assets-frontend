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

package mapping

import models.assets.SharesType
import models.{ShareClass, UserAnswers}
import pages.QuestionPage
import pages.asset.shares._
import play.api.libs.json.{JsSuccess, Reads}
import play.api.libs.functional.syntax._
import utils.Constants.{QUOTED, UNQUOTED}

class ShareAssetMapper extends Mapper[SharesType] {

  def apply(answers: UserAnswers): Option[SharesType] = {
    val readFromUserAnswers: Reads[SharesType] =
      SharesInAPortfolioPage(0).path.read[Boolean].flatMap {
        case true  => readPortfolio
        case false => readNonPortfolio
      }

    mapAnswersWithExplicitReads(answers, readFromUserAnswers)
  }

  private def readPortfolio: Reads[SharesType] =
    (
      readStringToLong(SharePortfolioQuantityInTrustPage(0)) and
        SharePortfolioNamePage(0).path.read[String] and
        Reads(_ => JsSuccess(ShareClass.toDES(ShareClass.Other))) and
        onStockExchange(SharePortfolioOnStockExchangePage(0)) and
        SharePortfolioValueInTrustPage(0).path.read[Long] and
        Reads(_ => JsSuccess(Some(true))) and
        Reads(_ => JsSuccess(Some(ShareClass.Other)))
    )(SharesType.apply _)

  private def readNonPortfolio: Reads[SharesType] =
    (
      readStringToLong(ShareQuantityInTrustPage(0)) and
        ShareCompanyNamePage(0).path.read[String] and
        ShareClassPage(0).path.read[ShareClass].map(ShareClass.toDES) and
        onStockExchange(SharesOnStockExchangePage(0)) and
        ShareValueInTrustPage(0).path.read[Long] and
        Reads(_ => JsSuccess(Some(false))) and
        ShareClassPage(0).path.read[ShareClass].map(Some(_))
    )(SharesType.apply _)

  private def readStringToLong(page: QuestionPage[Long]): Reads[String] =
    page.path.read[Long].map(_.toString)

  private def onStockExchange(page: QuestionPage[Boolean]): Reads[String] =
    page.path.read[Boolean].flatMap {
      case true  => Reads(_ => JsSuccess(QUOTED))
      case false => Reads(_ => JsSuccess(UNQUOTED))
    }

}
