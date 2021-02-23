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

import models.WhatKindOfAsset.PropertyOrLand
import models.{Address, WhatKindOfAsset}
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsSuccess, Reads, __}

final case class PropertyOrLandAsset(override val whatKindOfAsset: WhatKindOfAsset,
                                     propertyOrLandDescription: Option[String],
                                     address: Option[Address],
                                     propertyLandValueTrust: Option[Long],
                                     propertyOrLandTotalValue: Long) extends Asset

object PropertyOrLandAsset {

  implicit lazy val reads: Reads[PropertyOrLandAsset] = {

    val optionalAddressReads: Reads[Option[Address]] = {
      (__ \ PropertyOrLandUKAddressPage.key).read[Address].map(Some(_): Option[Address]) orElse
        (__ \ PropertyOrLandInternationalAddressPage.key).read[Address].map(Some(_): Option[Address]) orElse
        Reads(_ => JsSuccess(None: Option[Address]))
    }

    val landOrPropertyReads: Reads[PropertyOrLandAsset] = (
      (__ \ PropertyOrLandDescriptionPage.key).readNullable[String] and
        optionalAddressReads and
        (__ \ PropertyLandValueTrustPage.key).readNullable[Long] and
        (__ \ PropertyOrLandTotalValuePage.key).read[Long] and
        (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset]
      )((description, address, value, totalValue, kind) => PropertyOrLandAsset(kind, description, address, value, totalValue))

    (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset].flatMap[WhatKindOfAsset] {
      whatKindOfAsset: WhatKindOfAsset =>
        if (whatKindOfAsset == PropertyOrLand) {
          Reads(_ => JsSuccess(whatKindOfAsset))
        } else {
          Reads(_ => JsError("PropertyOrLand asset must be of type `PropertyOrLand`"))
        }
    }.andKeep(landOrPropertyReads)

  }
}