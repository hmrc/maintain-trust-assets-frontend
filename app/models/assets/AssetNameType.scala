/*
 * Copyright 2026 HM Revenue & Customs
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

package models.assets

import play.api.libs.json.{JsString, Writes}

sealed trait AssetNameType {
  def toString(): String
}

object AssetNameType {

  case object MoneyAssetNameType extends AssetNameType {
    override def toString(): String = "MoneyAsset"
  }

  case object PropertyOrLandAssetNameType extends AssetNameType {
    override def toString(): String = "PropertyOrLandAsset"
  }

  case object SharesAssetNameType extends AssetNameType {
    override def toString(): String = "SharesAsset"
  }

  case object BusinessAssetNameType extends AssetNameType {
    override def toString(): String = "BusinessAsset"
  }

  case object PartnershipAssetNameType extends AssetNameType {
    override def toString(): String = "PartnershipAsset"
  }

  case object OtherAssetNameType extends AssetNameType {
    override def toString(): String = "OtherAsset"
  }

  case object NonEeaBusinessAssetNameType extends AssetNameType {
    override def toString(): String = "NonEeaBusinessAsset"
  }

  val writesToTrusts: Writes[AssetNameType] = Writes {
    case MoneyAssetNameType          => JsString("monetary")
    case PropertyOrLandAssetNameType => JsString("propertyOrLand")
    case SharesAssetNameType         => JsString("shares")
    case BusinessAssetNameType       => JsString("business")
    case PartnershipAssetNameType    => JsString("partnerShip")
    case OtherAssetNameType          => JsString("other")
    case NonEeaBusinessAssetNameType => JsString("nonEEABusiness")
  }

}
