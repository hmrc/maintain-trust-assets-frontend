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

package mapping.reads

import java.time.LocalDate

import models.WhatKindOfAsset
import models.WhatKindOfAsset.Money
import pages.asset.WhatKindOfAssetPage
import pages.asset.money._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, _}

final case class MoneyAsset(override val whatKindOfAsset: WhatKindOfAsset,
                            value: Long,
                            startDate: LocalDate) extends Asset

object MoneyAsset {

  implicit lazy val reads: Reads[MoneyAsset] = (
    (__ \ WhatKindOfAssetPage).read[WhatKindOfAsset].filter(_ == Money) and
      (__ \ AssetMoneyValuePage).read[Long] and
      (__ \ StartDatePage).read[LocalDate]
    )(MoneyAsset.apply _)

}
