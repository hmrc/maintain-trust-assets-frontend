/*
 * Copyright 2024 HM Revenue & Customs
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

package connectors

import config.FrontendAppConfig

import javax.inject.Inject
import models._
import models.assets.{AssetMonetaryAmount, Assets, BusinessAssetType, NonEeaBusinessType, OtherAssetType, PartnershipType, PropertyLandType, SharesType}
import models.http.TaxableMigrationFlag
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import scala.concurrent.{ExecutionContext, Future}

class TrustsConnector @Inject()(http: HttpClientV2, config: FrontendAppConfig) extends Logging {

  private val trustsUrl: String = s"${config.trustsUrl}/trusts"
  private val assetsUrl: String = s"$trustsUrl/assets"

  def getTrustDetails(identifier: String)
                     (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    val url: String = s"$trustsUrl/trust-details/$identifier/transformed"
    http
      .get(url"$url")
      .execute[TrustDetails]
  }

  def getAssets(identifier: String)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Assets] = {
    val url: String = s"$assetsUrl/$identifier/transformed"
    http
      .get(url"$url")
      .execute[Assets]
  }

  def addMoneyAsset(identifier: String, asset: AssetMonetaryAmount)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/add-money/$identifier"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def amendMoneyAsset(identifier: String, index: Int, asset: AssetMonetaryAmount)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/amend-money/$identifier/$index"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def addPropertyOrLandAsset(identifier: String, asset: PropertyLandType)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/add-property-or-land/$identifier"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def amendPropertyOrLandAsset(identifier: String, index: Int, asset: PropertyLandType)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/amend-property-or-land/$identifier/$index"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def addSharesAsset(identifier: String, asset: SharesType)
                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/add-shares/$identifier"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def amendSharesAsset(identifier: String, index: Int, asset: SharesType)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/amend-shares/$identifier/$index"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def addBusinessAsset(identifier: String, asset: BusinessAssetType)
                    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/add-business/$identifier"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def amendBusinessAsset(identifier: String, index: Int, asset: BusinessAssetType)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/amend-business/$identifier/$index"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def addPartnershipAsset(index: Int, identifier: String, asset: PartnershipType)
                         (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/add-partnership/$identifier"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def amendPartnershipAsset(identifier: String, index: Int, asset: PartnershipType)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/amend-partnership/$identifier/$index"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def addOtherAsset(identifier: String, asset: OtherAssetType)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/add-other/$identifier"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def amendOtherAsset(identifier: String, index: Int, asset: OtherAssetType)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/amend-other/$identifier/$index"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def addNonEeaBusinessAsset(identifier: String, asset: NonEeaBusinessType)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/add-non-eea-business/$identifier"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def amendNonEeaBusinessAsset(identifier: String, index: Int, asset: NonEeaBusinessType)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/amend-non-eea-business/$identifier/$index"
    http
      .post(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def removeAsset(identifier: String, asset: RemoveAsset)
                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val url: String = s"$assetsUrl/$identifier/remove"
    http
      .put(url"$url")
      .withBody(Json.toJson(asset))
      .execute[HttpResponse]
  }

  def isTrust5mld(identifier: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val url: String = s"$trustsUrl/$identifier/is-trust-5mld"
    http
      .get(url"$url")
      .execute[Boolean]
  }

  def getTrustMigrationFlag(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TaxableMigrationFlag] = {
    val url = s"$trustsUrl/$identifier/taxable-migration/migrating-to-taxable"
    http
      .get(url"$url")
      .execute[TaxableMigrationFlag]
  }

}
