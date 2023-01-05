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

package viewmodels

import models.Status.InProgress
import models.WhatKindOfAsset.PropertyOrLand
import models.{NonUkAddress, Status, UkAddress, WhatKindOfAsset}
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._
import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class PropertyOrLandAssetViewModel(`type`: WhatKindOfAsset,
                                              hasAddress: Option[Boolean],
                                              address: Option[String],
                                              description: Option[String],
                                              status: Status) extends AssetViewModel {

  override val label: Option[String] = if (address.isDefined) address else description
}

object PropertyOrLandAssetViewModel extends AssetViewModelReads {

  implicit lazy val reads: Reads[PropertyOrLandAssetViewModel] = {

    val addressLine1Reads: Reads[Option[String]] =
      (__ \ PropertyOrLandUKAddressPage).read[UkAddress].map(_.toLine1Option) orElse
        (__ \ PropertyOrLandInternationalAddressPage).read[NonUkAddress].map(_.toLine1Option) orElse
        Reads(_ => JsSuccess(None))

    ((__ \ WhatKindOfAssetPage).read[WhatKindOfAsset].filter(_ == PropertyOrLand) and
      (__ \ PropertyOrLandAddressYesNoPage).readNullable[Boolean] and
      addressLine1Reads and
      (__ \ PropertyOrLandDescriptionPage).readNullable[String] and
      (__ \ AssetStatus).readWithDefault[Status](InProgress)
      )(PropertyOrLandAssetViewModel.apply _)

  }
}
