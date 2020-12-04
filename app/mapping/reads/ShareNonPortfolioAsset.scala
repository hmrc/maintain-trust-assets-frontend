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

import models.WhatKindOfAsset.Shares
import models.{ShareClass, Status, WhatKindOfAsset}
import pages.asset.WhatKindOfAssetPage
import pages.asset.shares._
import play.api.libs.json._

final case class ShareNonPortfolioAsset(override val whatKindOfAsset: WhatKindOfAsset,
                                        listedOnTheStockExchange: Boolean,
                                        shareCompanyName: String,
                                        sharesInAPortfolio: Boolean,
                                        quantityInTheTrust: String,
                                        value: Long,
                                        `class`: ShareClass,
                                        status: Status) extends Asset with ShareAsset {

  val quoted: String = quotedOrUnquoted(listedOnTheStockExchange)
}

object ShareNonPortfolioAsset {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[ShareNonPortfolioAsset] = {

    val shareReads : Reads[ShareNonPortfolioAsset] = Json.reads[ShareNonPortfolioAsset]

    (
      (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset] and
      (__ \ SharesInAPortfolioPage.key).read[Boolean]
    )((_, _)).flatMap[(WhatKindOfAsset, Boolean)] {
      case (whatKindOfAsset, portfolio) =>
        if (whatKindOfAsset == Shares && !portfolio) {
          Reads(_ => JsSuccess((whatKindOfAsset, portfolio)))
        } else {
          Reads(_ => JsError("share asset must be of type `Shares`"))
        }
    }.andKeep(shareReads)

  }

}
