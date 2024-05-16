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

package viewmodels

import models.Status.InProgress
import models.WhatKindOfAsset.Partnership
import models.{Status, WhatKindOfAsset}
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.partnership.PartnershipDescriptionPage
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class PartnershipAssetViewModel(`type`: WhatKindOfAsset,
                                           description: Option[String],
                                           status: Status) extends AssetViewModel {

  override val label: Option[String] = description
}

object PartnershipAssetViewModel {

  implicit lazy val reads: Reads[PartnershipAssetViewModel] = (
    (__ \ WhatKindOfAssetPage).read[WhatKindOfAsset].filter(_ == Partnership) and
      (__ \ PartnershipDescriptionPage).readNullable[String] and
      (__ \ AssetStatus).readWithDefault[Status](InProgress)
    )(PartnershipAssetViewModel.apply _)

}
