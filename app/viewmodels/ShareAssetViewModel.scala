/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class ShareAssetViewModel(`type`: WhatKindOfAsset,
                                     name: Option[String],
                                     status: Status) extends AssetViewModel {

  override val label: Option[String] = name
}

object ShareAssetViewModel extends AssetViewModelReads {



  implicit lazy val reads: Reads[ShareAssetViewModel] = {

    val nameReads : Reads[Option[String]] =
      (__ \ SharePortfolioNamePage.key).read[String].map(_.toOption) orElse
        (__ \ ShareCompanyNamePage.key).readNullable[String]

    ((__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset].filter(_ == Shares) and
      nameReads and
      (__ \ AssetStatus.key).readWithDefault[Status](InProgress)
      )(ShareAssetViewModel.apply _)

  }
}
