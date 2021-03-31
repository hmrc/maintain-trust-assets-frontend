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

package connectors

import config.FrontendAppConfig
import javax.inject.Inject
import models.{Assets, TrustDetails}
import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

class TrustsConnector @Inject()(http: HttpClient, config: FrontendAppConfig) extends Logging {

  private val trustsUrl: String = s"${config.trustsUrl}/trusts"
  private val assetsUrl: String = s"$trustsUrl/assets"

  def getTrustDetails(identifier: String)
                     (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[TrustDetails] = {
    val url: String = s"$trustsUrl/$identifier/trust-details"
    http.GET[TrustDetails](url)
  }

  def getAssets(identifier: String)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Assets] = {
    val url: String = s"$assetsUrl/$identifier/transformed"
    logger.info(s"---- getAssets")
    logger.info(s"---- assets json ${http.GET[String](url)}")
    http.GET[Assets](url)
  }

  def isTrust5mld(identifier: String)
                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val url: String = s"$trustsUrl/$identifier/is-trust-5mld"
    http.GET[Boolean](url)
  }

}
