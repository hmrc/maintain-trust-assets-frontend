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

import models.WhatKindOfAsset.NonEeaBusiness
import models.{Address, WhatKindOfAsset}
import pages.asset.WhatKindOfAssetPage
import pages.asset.noneeabusiness._
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.LocalDate

final case class NonEeaBusinessAsset(override val whatKindOfAsset: WhatKindOfAsset,
                                     name: String,
                                     address: Address,
                                     governingCountry: String,
                                     startDate: LocalDate) extends Asset {

  override val arg: String = name
}

object NonEeaBusinessAsset {

  implicit lazy val reads: Reads[NonEeaBusinessAsset] = (
    (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset].filter(_ == NonEeaBusiness) and
      (__ \ NamePage.key).read[String] and
      (__ \ InternationalAddressPage.key).read[Address] and
      (__ \ GoverningCountryPage.key).read[String] and
      (__ \ StartDatePage.key).read[LocalDate]
    )(NonEeaBusinessAsset.apply _)

}
