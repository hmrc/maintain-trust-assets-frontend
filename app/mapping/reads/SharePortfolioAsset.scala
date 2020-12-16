/*
 * Copyright 2020 HM Revenue & Customs
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

import models.WhatKindOfAsset
import models.WhatKindOfAsset.Shares
import pages.asset.WhatKindOfAssetPage
import pages.asset.shares._
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class SharePortfolioAsset(override val whatKindOfAsset: WhatKindOfAsset,
                                     override val listedOnTheStockExchange: Boolean,
                                     override val name: String,
                                     sharesInAPortfolio: Boolean,
                                     quantityInTheTrust: String,
                                     value: Long) extends ShareAsset

object SharePortfolioAsset {

  implicit lazy val reads: Reads[SharePortfolioAsset] = {

    val shareReads: Reads[SharePortfolioAsset] = (
      (__ \ SharePortfolioNamePage.key).read[String] and
        (__ \ SharePortfolioOnStockExchangePage.key).read[Boolean] and
        (__ \ SharePortfolioQuantityInTrustPage.key).read[String] and
        (__ \ SharePortfolioValueInTrustPage.key).read[Long] and
        (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset]
      )((name, listedOnStockExchange, quantity, value, kind) => SharePortfolioAsset(kind, listedOnStockExchange, name, sharesInAPortfolio = true, quantity, value))

    (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset].flatMap[WhatKindOfAsset] {
      whatKindOfAsset: WhatKindOfAsset =>
        if (whatKindOfAsset == Shares) {
          Reads(_ => JsSuccess(whatKindOfAsset))
        } else {
          Reads(_ => JsError("share portfolio asset must be of type `Shares`"))
        }
    }.andKeep(shareReads)

  }

}
