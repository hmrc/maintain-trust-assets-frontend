/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import com.google.inject.ImplementedBy
import connectors.TrustsConnector
import javax.inject.Inject
import models._
import models.assets.{AssetMonetaryAmount, Assets, BusinessAssetType, NonEeaBusinessType, OtherAssetType, PartnershipType, PropertyLandType, SharesType}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class TrustServiceImpl @Inject()(connector: TrustsConnector) extends TrustService {

  override def getAssets(identifier: String)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[Assets] =
    connector.getAssets(identifier)

  override def getMonetaryAsset(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Option[AssetMonetaryAmount]] =
    getAssets(identifier).map(_.monetary.headOption)

  override def getPropertyOrLandAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[PropertyLandType] =
    getAssets(identifier).map(_.propertyOrLand(index))

  override def getSharesAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[SharesType] =
    getAssets(identifier).map(_.shares(index))

  override def getBusinessAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[BusinessAssetType] =
    getAssets(identifier).map(_.business(index))

  override def getOtherAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[OtherAssetType] =
    getAssets(identifier).map(_.other(index))

  override def getPartnershipAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[PartnershipType] =
    getAssets(identifier).map(_.partnerShip(index))

  override def getNonEeaBusinessAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[NonEeaBusinessType] =
    getAssets(identifier).map(_.nonEEABusiness(index))

  override def removeAsset(identifier: String, asset: RemoveAsset)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    connector.removeAsset(identifier, asset)
}

@ImplementedBy(classOf[TrustServiceImpl])
trait TrustService {

  def getAssets(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Assets]

  def getMonetaryAsset(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Option[AssetMonetaryAmount]]

  def getPropertyOrLandAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[PropertyLandType]

  def getSharesAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[SharesType]

  def getBusinessAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[BusinessAssetType]

  def getOtherAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[OtherAssetType]

  def getPartnershipAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[PartnershipType]

  def getNonEeaBusinessAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[NonEeaBusinessType]

  def removeAsset(identifier: String, asset: RemoveAsset)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]

}
