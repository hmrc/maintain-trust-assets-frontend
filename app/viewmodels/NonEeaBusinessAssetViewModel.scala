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

package viewmodels

import models.Status.InProgress
import models.WhatKindOfAsset.NonEeaBusiness
import models.{Status, WhatKindOfAsset}
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.noneeabusiness._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsError, JsSuccess, Reads, __}

final case class NonEeaBusinessAssetViewModel(`type`: WhatKindOfAsset,
                                              name: Option[String],
                                              override val status: Status) extends AssetViewModel

object NonEeaBusinessAssetViewModel {

  implicit lazy val reads: Reads[NonEeaBusinessAssetViewModel] = {

    val nonEeaBusinessReads: Reads[NonEeaBusinessAssetViewModel] = (
      (__ \ NamePage.key).readNullable[String] and
        (__ \ AssetStatus.key).readWithDefault[Status](InProgress)
      )((name, status) => NonEeaBusinessAssetViewModel(NonEeaBusiness, name, status))

    (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset].flatMap[WhatKindOfAsset] {
      case NonEeaBusiness =>
        Reads(_ => JsSuccess(NonEeaBusiness))
      case _ =>
        Reads(_ => JsError("non-EEA business asset must be of type `NonEeaBusiness`"))
    }.andKeep(nonEeaBusinessReads)
  }
}
