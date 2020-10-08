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
import models.{Status, WhatKindOfAsset}

final case class DefaultAssetsViewModel(`type` : WhatKindOfAsset,
                                        override val status : Status
                                       ) extends AssetViewModel


object DefaultAssetsViewModel {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit lazy val reads: Reads[DefaultAssetsViewModel] =
    ((__ \ "whatKindOfAsset").read[WhatKindOfAsset] and
      (__ \ "status").readWithDefault[Status](InProgress)
      )((kind, status) =>
      DefaultAssetsViewModel(kind, status)
    )
}