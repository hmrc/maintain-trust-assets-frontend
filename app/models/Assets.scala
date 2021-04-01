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

package models

import java.time.LocalDate

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed trait AssetType

case class Assets(monetary: List[AssetMonetaryAmount],
                  propertyOrLand: List[PropertyLandType],
                  shares: List[SharesType],
                  business: List[BusinessAssetType],
                  partnerShip: List[PartnershipType],
                  other: List[OtherAssetType],
                  nonEEABusiness: List[NonEeaBusinessType])


object Assets {
  implicit val reads: Reads[Assets] =
    ((__ \ "assets" \ "monetary").readWithDefault[List[AssetMonetaryAmount]](Nil)
      and (__ \ "assets" \ "propertyOrLand").readWithDefault[List[PropertyLandType]](Nil)
      and (__ \ "assets" \ "shares").readWithDefault[List[SharesType]](Nil)
      and (__ \ "assets" \ "business").readWithDefault[List[BusinessAssetType]](Nil)
      and (__ \ "assets" \ "partnerShip").readWithDefault[List[PartnershipType]](Nil)
      and (__ \ "assets" \ "other").readWithDefault[List[OtherAssetType]](Nil)
      and (__ \ "assets" \ "nonEEABusiness").readWithDefault[List[NonEeaBusinessType]](Nil)
      ).apply(Assets.apply _)
}


case class AssetMonetaryAmount(assetMonetaryAmount: Long) extends AssetType

object AssetMonetaryAmount {
  implicit val assetMonetaryAmountFormat: Format[AssetMonetaryAmount] = Json.format[AssetMonetaryAmount]
}

case class PropertyLandType(buildingLandName: Option[String],
                            address: Option[AddressType],
                            valueFull: Long,
                            valuePrevious: Option[Long]) extends AssetType

object PropertyLandType {
  implicit val propertyLandTypeFormat: Format[PropertyLandType] = Json.format[PropertyLandType]
}

case class BusinessAssetType(orgName: String,
                             businessDescription: String,
                             address: AddressType,
                             businessValue: Long) extends AssetType

object BusinessAssetType {
  implicit val businessAssetTypeFormat: Format[BusinessAssetType] = Json.format[BusinessAssetType]
}

case class OtherAssetType(description: String,
                          value: Long) extends AssetType

object OtherAssetType {
  implicit val otherAssetTypeFormat: Format[OtherAssetType] = Json.format[OtherAssetType]
}

case class PartnershipType(description: String,
                           partnershipStart: LocalDate) extends AssetType

object PartnershipType {

  implicit val partnershipTypeFormat: Format[PartnershipType] = Json.format[PartnershipType]
}

case class SharesType(numberOfShares: String,
                      orgName: String,
                      shareClass: String,
                      typeOfShare: String,
                      value: Long) extends AssetType

object SharesType {
  implicit val sharesTypeFormat: Format[SharesType] = Json.format[SharesType]
}

case class NonEeaBusinessType(orgName: String,
                              address: AddressType,
                              govLawCountry: String,
                              startDate: LocalDate) extends AssetType

object NonEeaBusinessType {
  implicit val format: Format[NonEeaBusinessType] = Json.format[NonEeaBusinessType]
}

case class AddressType(line1: String,
                       line2: String,
                       line3: Option[String],
                       line4: Option[String],
                       postCode: Option[String],
                       country: String)

object AddressType {
  implicit val addressTypeFormat: Format[AddressType] = Json.format[AddressType]
}
