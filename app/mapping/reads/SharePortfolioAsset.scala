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
import models.{Status, WhatKindOfAsset}
import play.api.libs.json._

final case class SharePortfolioAsset(override val whatKindOfAsset: WhatKindOfAsset,
                                     listedOnTheStockExchange: Boolean,
                                     name: String,
                                     sharesInAPortfolio: Boolean,
                                     quantityInTheTrust: String,
                                     value: String,
                                     status: Status
                                    ) extends Asset with ShareAsset {
  val quoted = if (this.listedOnTheStockExchange) "Quoted" else "Unquoted"
}

object SharePortfolioAsset {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[SharePortfolioAsset] = {

    val shareReads : Reads[SharePortfolioAsset] = Json.reads[SharePortfolioAsset]

    (
      (__ \ "whatKindOfAsset").read[WhatKindOfAsset] and
        (__ \ "sharesInAPortfolio").read[Boolean]
      )((_, _)).flatMap[(WhatKindOfAsset, Boolean)] {
      case (whatKindOfAsset, portfolio) =>
        if (whatKindOfAsset == Shares && portfolio) {
          Reads(_ => JsSuccess((whatKindOfAsset, portfolio)))
        } else {
          Reads(_ => JsError("share portfolio asset must be of type `Shares`"))
        }
    }.andKeep(shareReads)

  }

}



