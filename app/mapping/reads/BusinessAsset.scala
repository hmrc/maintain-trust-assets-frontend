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

import models.WhatKindOfAsset.Business
import models.{Address, WhatKindOfAsset}
import play.api.libs.json.{JsError, JsSuccess, Reads, __}

final case class BusinessAsset(override val whatKindOfAsset: WhatKindOfAsset,
                               assetName: String,
                               assetDescription: String,
                               address: Address,
                               currentValue: Long) extends Asset

object BusinessAsset {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[BusinessAsset] = {

    val businessAssetReads: Reads[BusinessAsset] = (
      (__ \ "whatKindOfAsset").read[WhatKindOfAsset] and
        (__ \ "name").read[String] and
        (__ \ "description").read[String] and
        readAddress() and
        (__ \ "value").read[Long]
      )((kind, name, description, address, value) => BusinessAsset(kind, name, description, address, value))

    (__ \ "whatKindOfAsset").read[WhatKindOfAsset].flatMap[WhatKindOfAsset] {
      whatKindOfAsset: WhatKindOfAsset =>
        if (whatKindOfAsset == Business) {
          Reads(_ => JsSuccess(whatKindOfAsset))
        } else {
          Reads(_ => JsError("Business asset must be of type `Business`"))
        }
    }.andKeep(businessAssetReads)

  }

  private def readAddress(): Reads[Address] = {
    (__ \ "ukAddress").read[Address] orElse
      (__ \ "internationalAddress").read[Address]
  }
}