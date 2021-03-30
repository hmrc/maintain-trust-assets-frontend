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

package models.assets

import play.api.libs.functional.syntax._
import play.api.libs.json._

trait Asset

case class Assets(businessAssets: List[BusinessAsset],
                  monetaryAssets: List[MonetaryAsset],
                  nonEEABusinessAssets: List[NonEEABusinessAsset],
                  partnershipAssets: List[PartnershipAsset],
                  propertyOrLandAssets: List[PropertyOrLandAsset],
                  sharesAssets: List[SharesAsset],
                  otherAssets: List[OtherAsset]
                 )

object Assets {
  implicit val reads: Reads[Assets] =
    ((__ \ "assets" \ "business").readWithDefault[List[BusinessAsset]](Nil)
      and (__ \ "assets" \ "monetary").readWithDefault[List[MonetaryAsset]](Nil)
      and (__ \ "assets" \ "nonEEABusiness").readWithDefault[List[NonEEABusinessAsset]](Nil)
      and (__ \ "assets" \ "partnerShip").readWithDefault[List[PartnershipAsset]](Nil)
      and (__ \ "assets" \ "propertyOrLand").readWithDefault[List[PropertyOrLandAsset]](Nil)
      and (__ \ "assets" \ "shares").readWithDefault[List[SharesAsset]](Nil)
      and (__ \ "assets" \ "other").readWithDefault[List[OtherAsset]](Nil)
      ).apply(Assets.apply _)
}