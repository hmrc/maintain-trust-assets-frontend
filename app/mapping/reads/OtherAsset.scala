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
import models.WhatKindOfAsset.Other
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsSuccess, Reads, __}

final case class OtherAsset(
                             override val whatKindOfAsset: WhatKindOfAsset,
                             description: String,
                             value: String
                           ) extends Asset

object OtherAsset {

  implicit lazy val reads: Reads[OtherAsset] = {

    val otherReads: Reads[OtherAsset] = (
      (__ \ "otherAssetDescription").read[String] and
        (__ \ "otherAssetValue").read[String] and
        (__ \ "whatKindOfAsset").read[WhatKindOfAsset]
      )((description, value, kind) => OtherAsset(kind, description, value))

    (__ \ "whatKindOfAsset").read[WhatKindOfAsset].flatMap[WhatKindOfAsset] {
      whatKindOfAsset: WhatKindOfAsset =>
        if (whatKindOfAsset == Other) {
          Reads(_ => JsSuccess(whatKindOfAsset))
        } else {
          Reads(_ => JsError("other asset must be of type `Other`"))
        }
    }.andKeep(otherReads)

  }

}
