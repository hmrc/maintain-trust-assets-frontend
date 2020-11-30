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
import models.WhatKindOfAsset.Shares
import models.{Status, WhatKindOfAsset}

final case class ShareAssetViewModel(`type` : WhatKindOfAsset,
                                     inPortfolio: Option[Boolean],
                                     name : Option[String],
                                     override val status : Status) extends AssetViewModel

object ShareAssetViewModel {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit class OptionString(s : String) {

    def toOption : Option[String] = if(s.isEmpty) None else Some(s)

  }

  implicit lazy val reads: Reads[ShareAssetViewModel] = {

    val nameReads : Reads[Option[String]] =
      (__ \ "name").read[String].map(_.toOption) orElse
        (__ \ "shareCompanyName").readNullable[String]

    val shareReads: Reads[ShareAssetViewModel] =
      (
        nameReads and
          (__ \ "status").readWithDefault[Status](InProgress) and
          (__ \ "sharesInAPortfolio").readNullable[Boolean]
        )((name, status, inPortfolio) =>
        ShareAssetViewModel(Shares, inPortfolio, name, status)
      )

    (__ \ "whatKindOfAsset").read[WhatKindOfAsset].flatMap[WhatKindOfAsset] {
      whatKindOfAsset: WhatKindOfAsset =>
        if (whatKindOfAsset == Shares) {
          Reads(_ => JsSuccess(whatKindOfAsset))
        } else {
          Reads(_ => JsError("share asset must be of type `Share`"))
        }
    }.andKeep(shareReads)

  }

}