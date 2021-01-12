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
import models.WhatKindOfAsset.Shares
import models.{Status, WhatKindOfAsset}
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.shares._

final case class ShareAssetViewModel(`type`: WhatKindOfAsset,
                                     name: Option[String],
                                     override val status: Status) extends AssetViewModel

object ShareAssetViewModel extends AssetViewModelReads {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit lazy val reads: Reads[ShareAssetViewModel] = {

    val nameReads : Reads[Option[String]] =
      (__ \ SharePortfolioNamePage.key).read[String].map(_.toOption) orElse
        (__ \ ShareCompanyNamePage.key).readNullable[String]

    val shareReads: Reads[ShareAssetViewModel] =
      (
        nameReads and
          (__ \ AssetStatus.key).readWithDefault[Status](InProgress)
        )((name, status) =>
        ShareAssetViewModel(Shares, name, status)
      )

    (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset].flatMap[WhatKindOfAsset] {
      whatKindOfAsset: WhatKindOfAsset =>
        if (whatKindOfAsset == Shares) {
          Reads(_ => JsSuccess(whatKindOfAsset))
        } else {
          Reads(_ => JsError("share asset must be of type `Share`"))
        }
    }.andKeep(shareReads)

  }

}