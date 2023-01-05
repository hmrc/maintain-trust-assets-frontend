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

package models.assets


import play.api.libs.functional.syntax._
import play.api.libs.json._

trait AssetType

case class Assets(monetary: List[AssetMonetaryAmount] = Nil,
                  propertyOrLand: List[PropertyLandType] = Nil,
                  shares: List[SharesType] = Nil,
                  business: List[BusinessAssetType] = Nil,
                  partnerShip: List[PartnershipType] = Nil,
                  other: List[OtherAssetType] = Nil,
                  nonEEABusiness: List[NonEeaBusinessType] = Nil) {

  def isEmpty: Boolean = this match {
    case Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil) => true
    case _ => false
  }
}

object Assets {
  implicit val reads: Reads[Assets] = (
    (__ \ "assets" \ "monetary").readWithDefault[List[AssetMonetaryAmount]](Nil)
      and (__ \ "assets" \ "propertyOrLand").readWithDefault[List[PropertyLandType]](Nil)
      and (__ \ "assets" \ "shares").readWithDefault[List[SharesType]](Nil)
      and (__ \ "assets" \ "business").readWithDefault[List[BusinessAssetType]](Nil)
      and (__ \ "assets" \ "partnerShip").readWithDefault[List[PartnershipType]](Nil)
      and (__ \ "assets" \ "other").readWithDefault[List[OtherAssetType]](Nil)
      and (__ \ "assets" \ "nonEEABusiness").readWithDefault[List[NonEeaBusinessType]](Nil)
    ).apply(Assets.apply _)
}
