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

import models.WhatKindOfAsset.PropertyOrLand
import models.{Address, WhatKindOfAsset}
import play.api.libs.json.{JsError, JsSuccess, Reads, __}

final case class PropertyOrLandAsset(override val whatKindOfAsset: WhatKindOfAsset,
                                     propertyOrLandDescription: Option[String],
                                     address: Option[Address],
                                     propertyLandValueTrust: Option[String],
                                     propertyOrLandTotalValue: String) extends Asset

object PropertyOrLandAsset {

  import play.api.libs.functional.syntax._

  implicit lazy val reads: Reads[PropertyOrLandAsset] = {

    val landOrPropertyReads: Reads[PropertyOrLandAsset] = (
      (__ \ "propertyOrLandDescription").readNullable[String] and
        readAddress() and
        (__ \ "propertyOrLandValueTrust").readNullable[String] and
        (__ \ "propertyOrLandTotalValue").read[String] and
        (__ \ "whatKindOfAsset").read[WhatKindOfAsset]
      )((description, address, value, totalValue, kind) => PropertyOrLandAsset(kind, description, address, value, totalValue))

    (__ \ "whatKindOfAsset").read[WhatKindOfAsset].flatMap[WhatKindOfAsset] {
      whatKindOfAsset: WhatKindOfAsset =>
        if (whatKindOfAsset == PropertyOrLand) {
          Reads(_ => JsSuccess(whatKindOfAsset))
        } else {
          Reads(_ => JsError("PropertyOrLand asset must be of type `PropertyOrLand`"))
        }
    }.andKeep(landOrPropertyReads)

  }

  private def readAddress(): Reads[Option[Address]] = {
    (__ \ "ukAddress").readNullable[Address] orElse
      (__ \ "internationalAddress").readNullable[Address]
  }
}