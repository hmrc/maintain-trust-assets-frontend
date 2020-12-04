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
import models.WhatKindOfAsset.PropertyOrLand
import models.{InternationalAddress, Status, UKAddress, WhatKindOfAsset}
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._

final case class PropertyOrLandAssetViewModel(`type`: WhatKindOfAsset,
                                              hasAddress: Option[Boolean],
                                              address: Option[String],
                                              description: Option[String],
                                              override val status: Status) extends AssetViewModel

object PropertyOrLandAssetViewModel extends AssetViewModelReads {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit lazy val reads: Reads[PropertyOrLandAssetViewModel] = {

    val addressReads : Reads[Option[String]] =
      (__ \ PropertyOrLandUKAddressPage.key).read[UKAddress].map(_.toLine1) orElse
        (__ \ PropertyOrLandInternationalAddressPage.key).read[InternationalAddress].map(_.toLine1) orElse
        Reads(_ => JsSuccess(None))

    val propertyOrLandReads: Reads[PropertyOrLandAssetViewModel] =
      (
        (__ \ PropertyOrLandAddressYesNoPage.key).readNullable[Boolean] and
          addressReads and
          (__ \ PropertyOrLandDescriptionPage.key).readNullable[String] and
          (__ \ AssetStatus.key).readWithDefault[Status](InProgress)
        )((hasAddress, address, description, status) =>
        PropertyOrLandAssetViewModel(PropertyOrLand, hasAddress, address, description, status)
      )

    (__ \ WhatKindOfAssetPage.key).read[WhatKindOfAsset].flatMap[WhatKindOfAsset] {
      whatKindOfAsset: WhatKindOfAsset =>
        if (whatKindOfAsset == PropertyOrLand) {
          Reads(_ => JsSuccess(whatKindOfAsset))
        } else {
          Reads(_ => JsError("property or land asset must be of type `PropertyOrLand`"))
        }
    }.andKeep(propertyOrLandReads)
  }

}
