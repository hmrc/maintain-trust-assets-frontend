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

sealed trait PropertyOrLandAssetViewModel extends AssetViewModel

final case class PropertyOrLandAssetUKAddressViewModel(`type` : WhatKindOfAsset,
                                                       address : Option[String],
                                                       override val status : Status) extends PropertyOrLandAssetViewModel

final case class PropertyOrLandAssetInternationalAddressViewModel(`type` : WhatKindOfAsset,
                                                                  address : Option[String],
                                                                  override val status : Status) extends PropertyOrLandAssetViewModel

final case class PropertyOrLandAssetAddressViewModel(`type` : WhatKindOfAsset,
                                                     address : Option[String],
                                                     override val status : Status) extends PropertyOrLandAssetViewModel

final case class PropertyOrLandAssetDescriptionViewModel(`type` : WhatKindOfAsset,
                                                         description : Option[String],
                                                         override val status : Status) extends PropertyOrLandAssetViewModel

final case class PropertyOrLandDefaultViewModel(`type`: WhatKindOfAsset,
                                                override val status: Status) extends PropertyOrLandAssetViewModel

object PropertyOrLandAssetViewModel {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit lazy val reads: Reads[PropertyOrLandAssetViewModel] = {

    val ukReads: Reads[PropertyOrLandAssetViewModel] =
    {
      (__ \ "propertyOrLandAddressYesNo").read[Boolean].filter(x => x).flatMap { _ =>
        (__ \ "propertyOrLandAddressUKYesNo").read[Boolean].filter(x => x).flatMap { _ =>
          ((__ \ "ukAddress").readNullable[UKAddress].map(_.map(_.line1)) and
            (__ \ "status").readWithDefault[Status](InProgress)
            ) ((address, status) => {
            PropertyOrLandAssetUKAddressViewModel(
              PropertyOrLand,
              address,
              status)
          })
        }
      }
    }

    val internationalReads: Reads[PropertyOrLandAssetViewModel] =
    {
      (__ \ "propertyOrLandAddressYesNo").read[Boolean].filter(x => x).flatMap { _ =>
        (__ \ "propertyOrLandAddressUKYesNo").read[Boolean].filter(x => !x).flatMap { _ =>
          ((__ \ "internationalAddress").readNullable[InternationalAddress].map(_.map(_.line1)) and
            (__ \ "status").readWithDefault[Status](InProgress)
            ) ((address, status) => {
            PropertyOrLandAssetInternationalAddressViewModel(
              PropertyOrLand,
              address,
              status)
          })
        }
      }
    }

    val addressReads: Reads[PropertyOrLandAssetViewModel] =
    {
      (__ \ "propertyOrLandAddressYesNo").read[Boolean].filter{ x => x }.map { _ =>
      {
        PropertyOrLandAssetAddressViewModel(
          PropertyOrLand,
          None,
          InProgress)
      }
      }
    }

    val descriptionReads: Reads[PropertyOrLandAssetViewModel] = {
      (__ \ "propertyOrLandAddressYesNo").read[Boolean].filter(x => !x).flatMap { _ =>
        ((__ \ "propertyOrLandDescription").readNullable[String] and
          (__ \ "status").readWithDefault[Status](InProgress)
          ) ((description, status) => {
          PropertyOrLandAssetDescriptionViewModel(
            PropertyOrLand,
            description,
            status)
        })
      }
    }

    val defaultReads : Reads[PropertyOrLandAssetViewModel] = {
      (__ \ "whatKindOfAsset").read[WhatKindOfAsset].filter(x => x == PropertyOrLand).flatMap {
        kind =>
          (__ \ "status").readWithDefault[Status](InProgress).map {
            status =>
              PropertyOrLandDefaultViewModel(kind, status)
          }
      }
    }

    ukReads orElse internationalReads orElse addressReads orElse descriptionReads orElse defaultReads

  }

}
