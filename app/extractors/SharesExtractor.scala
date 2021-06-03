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

package extractors

import models.assets.SharesType
import models.{ShareClass, UserAnswers}
import pages.QuestionPage
import pages.asset.shares._
import pages.asset.shares.amend.IndexPage
import play.api.libs.json.JsPath
import utils.Constants.QUOTED

import scala.util.Try

class SharesExtractor extends AssetExtractor[SharesType] {

  override def apply(answers: UserAnswers,
                     assetType: SharesType,
                     index: Int): Try[UserAnswers] = {

    super.apply(answers, assetType, index)
      assetType.isPortfolio match {
        case Some(true) =>
          answers.set(SharePortfolioQuantityInTrustPage, assetType.numberOfShares.toLong)
            .flatMap(_.set(SharePortfolioNamePage, assetType.orgName))
            .flatMap(_.set(ShareClassPage, ShareClass.fromDES(assetType.shareClass)))
            .flatMap(_.set(SharePortfolioOnStockExchangePage, assetType.typeOfShare == QUOTED))
            .flatMap(_.set(SharePortfolioValueInTrustPage, assetType.value))

        case Some(false) =>
          answers.set(SharePortfolioQuantityInTrustPage, assetType.numberOfShares.toLong)
            .flatMap(_.set(SharePortfolioNamePage, assetType.orgName))
            .flatMap(_.set(ShareClassPage, ShareClass.fromDES(assetType.shareClass)))
            .flatMap(_.set(SharesOnStockExchangePage, assetType.typeOfShare == QUOTED))
            .flatMap(_.set(ShareValueInTrustPage, assetType.value))

        case None => Try(answers)
      }
  }

  override def indexPage: QuestionPage[Int] = IndexPage

  override def basePath: JsPath = pages.asset.shares.basePath
}
