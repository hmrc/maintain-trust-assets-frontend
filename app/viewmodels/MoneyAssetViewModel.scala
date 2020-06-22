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

package viewmodels

import models.Status.InProgress
import models.WhatKindOfAsset.Money
import models.{Status, WhatKindOfAsset}

final case class MoneyAssetViewModel(`type` : WhatKindOfAsset,
                                     value : Option[String],
                                     override val status : Status) extends AssetViewModel

object MoneyAssetViewModel {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit lazy val reads: Reads[MoneyAssetViewModel] = {

    def formatValue(v : String) = s"£$v"

    val moneyReads: Reads[MoneyAssetViewModel] =
      ((__ \ "assetMoneyValue").readNullable[String] and
        (__ \ "status").readWithDefault[Status](InProgress)
        )((value, status) => MoneyAssetViewModel(Money, value.map(formatValue), status))

    (__ \ "whatKindOfAsset").read[WhatKindOfAsset].flatMap[WhatKindOfAsset] {
      whatKindOfAsset: WhatKindOfAsset =>
        if (whatKindOfAsset == Money) {
          Reads(_ => JsSuccess(whatKindOfAsset))
        } else {
          Reads(_ => JsError("money asset must be of type `Money`"))
        }
    }.andKeep(moneyReads)

  }

}