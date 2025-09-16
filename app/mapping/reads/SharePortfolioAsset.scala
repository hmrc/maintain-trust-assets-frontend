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

package mapping.reads

import java.time.LocalDate

import models.WhatKindOfAsset
import models.WhatKindOfAsset.Shares
import pages.asset.WhatKindOfAssetPage
import pages.asset.shares._
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class SharePortfolioAsset(override val whatKindOfAsset: WhatKindOfAsset,
                                     sharesInAPortfolio: Boolean,
                                     override val name: String,
                                     override val listedOnTheStockExchange: Boolean,
                                     override val quantityInTheTrust: Long,
                                     value: Long,
                                     startDate: LocalDate) extends ShareAsset

object SharePortfolioAsset {

  implicit lazy val reads: Reads[SharePortfolioAsset] = (
    (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset].filter(_ == Shares) and
      Reads(_ => JsSuccess(true)) and
      (__ \ SharePortfolioNamePage.key).read[String] and
      (__ \ SharePortfolioOnStockExchangePage.key).read[Boolean] and
      (__ \ SharePortfolioQuantityInTrustPage.key).read[Long] and
      (__ \ SharePortfolioValueInTrustPage.key).read[Long] and
      (__ \ StartDatePage).read[LocalDate]
    )(SharePortfolioAsset.apply _)

}
