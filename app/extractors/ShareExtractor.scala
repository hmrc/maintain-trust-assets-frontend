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

package extractors

import models.assets.SharesType
import models.{ShareClass, UserAnswers}
import pages.QuestionPage
import pages.asset.shares._
import pages.asset.shares.amend.IndexPage
import play.api.libs.json.JsPath
import utils.Constants.QUOTED

import scala.util.Try

class ShareExtractor extends AssetExtractor[SharesType] {

  override def apply(answers: UserAnswers, assetType: SharesType, index: Int): Try[UserAnswers] =

    super.apply(answers, assetType, index).flatMap { updatedAnswers =>
      assetType.isPortfolio match {
        case Some(true) =>
          populatePages(
            updatedAnswers,
            assetType,
            index,
            SharePortfolioQuantityInTrustPage(0),
            SharePortfolioNamePage(0),
            SharePortfolioOnStockExchangePage(0),
            SharePortfolioValueInTrustPage(0)
          )
        case _          =>
          populatePages(
            updatedAnswers,
            assetType,
            index,
            ShareQuantityInTrustPage(0),
            ShareCompanyNamePage(0),
            SharesOnStockExchangePage(0),
            ShareValueInTrustPage(0)
          )
      }
    }

  private def populatePages(
    answers: UserAnswers,
    assetType: SharesType,
    index: Int,
    quantityPage: QuestionPage[Long],
    namePage: QuestionPage[String],
    stockExchangePage: QuestionPage[Boolean],
    valuePage: QuestionPage[Long]
  ): Try[UserAnswers] = {

    val shareClass = assetType.shareClassDisplay.getOrElse(ShareClass.fromDES(assetType.shareClass))

    answers
      .set(quantityPage, assetType.numberOfShares.toLong)
      .flatMap(_.set(namePage, assetType.orgName))
      .flatMap(_.set(SharesInAPortfolioPage(index), assetType.isPortfolio.getOrElse(false)))
      .flatMap(_.set(ShareClassPage(0), shareClass))
      .flatMap(_.set(stockExchangePage, assetType.typeOfShare == QUOTED))
      .flatMap(_.set(valuePage, assetType.value))
  }

  override def indexPage: QuestionPage[Int] = IndexPage

  override def basePath: JsPath = pages.asset.shares.basePath
}
