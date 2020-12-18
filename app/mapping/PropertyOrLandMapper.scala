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

package mapping

import mapping.reads.PropertyOrLandAsset
import models.PropertyLandType

import javax.inject.Inject

class PropertyOrLandMapper @Inject()(addressMapper: AddressMapper) extends Mapping[List[PropertyLandType], PropertyOrLandAsset] {

  override def mapAssets(assets: List[PropertyOrLandAsset]): List[PropertyLandType] = {
    assets.map { x =>
      val totalValue: Long = x.propertyOrLandTotalValue

      PropertyLandType(
        x.propertyOrLandDescription,
        addressMapper.build(x.address),
        totalValue,
        x.propertyLandValueTrust.getOrElse(totalValue)
      )
    }
  }
}