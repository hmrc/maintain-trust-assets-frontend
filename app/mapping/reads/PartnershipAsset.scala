/*
 * Copyright 2023 HM Revenue & Customs
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
import models.WhatKindOfAsset.Partnership
import pages.asset.WhatKindOfAssetPage
import pages.asset.partnership._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, __}

import java.time.LocalDate

final case class PartnershipAsset(override val whatKindOfAsset: WhatKindOfAsset,
                                  description: String,
                                  partnershipStartDate: LocalDate,
                                  startDate: LocalDate) extends Asset

object PartnershipAsset {

  implicit lazy val reads: Reads[PartnershipAsset] = (
    (__ \ WhatKindOfAssetPage).read[WhatKindOfAsset].filter(_ == Partnership) and
      (__ \ PartnershipDescriptionPage).read[String] and
      (__ \ PartnershipStartDatePage).read[LocalDate] and
      (__ \ StartDatePage).read[LocalDate]
    )(PartnershipAsset.apply _)

}
