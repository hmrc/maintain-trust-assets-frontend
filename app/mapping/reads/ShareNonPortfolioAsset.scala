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

package mapping.reads

import models.WhatKindOfAsset.Shares
import models.{ShareClass, WhatKindOfAsset}
import pages.asset.WhatKindOfAssetPage
import pages.asset.shares._
import play.api.libs.json._

final case class ShareNonPortfolioAsset(override val whatKindOfAsset: WhatKindOfAsset,
                                        override val listedOnTheStockExchange: Boolean,
                                        override val name: String,
                                        sharesInAPortfolio: Boolean,
                                        override val quantityInTheTrust: Long,
                                        value: Long,
                                        `class`: ShareClass) extends ShareAsset

object ShareNonPortfolioAsset {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[ShareNonPortfolioAsset] = {

    val shareReads: Reads[ShareNonPortfolioAsset] = (
      (__ \ ShareCompanyNamePage.key).read[String] and
        (__ \ SharesOnStockExchangePage.key).read[Boolean] and
        (__ \ ShareClassPage.key).read[ShareClass] and
        (__ \ ShareQuantityInTrustPage.key).read[Long] and
        (__ \ ShareValueInTrustPage.key).read[Long] and
        (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset]
      )((name, listedOnStockExchange, `class`, quantity, value, kind) => ShareNonPortfolioAsset(kind, listedOnStockExchange, name, sharesInAPortfolio = false, quantity, value, `class`))

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
