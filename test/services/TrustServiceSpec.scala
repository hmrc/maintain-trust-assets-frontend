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

package services

import java.time.LocalDate

import connectors.TrustsConnector
import models._
import models.assets.{AddressType, AssetMonetaryAmount, Assets, BusinessAssetType, NonEeaBusinessType, OtherAssetType, PartnershipType, PropertyLandType, SharesType}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FreeSpec, MustMatchers}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class TrustServiceSpec() extends FreeSpec with MockitoSugar with MustMatchers with ScalaFutures {

  val mockConnector: TrustsConnector = mock[TrustsConnector]
  val date: LocalDate = LocalDate.parse("2019-02-03")

  val moneyAsset = AssetMonetaryAmount(123)
  val propertyOrLandAsset = PropertyLandType(None, None, 123, None)
  val sharesAsset = SharesType("", "", "", "", 123)
  val businessAsset = BusinessAssetType("", "", AddressType("", "", None, None, None, ""), 123)
  val partnershipAsset = PartnershipType("", LocalDate.now)
  val otherAsset = OtherAssetType("", 123)
  val nonEeaBusinessAsset = NonEeaBusinessType(None, "orgName", AddressType("", "", None, None, None, ""), "", LocalDate.now, None)

  val assets = Assets(
    monetary = List(moneyAsset),
    propertyOrLand = List(propertyOrLandAsset),
    shares = List(sharesAsset),
    business = List(businessAsset),
    partnerShip = List(partnershipAsset),
    other = List(otherAsset),
    nonEEABusiness = List(nonEeaBusinessAsset)
  )

  "Trust service" - {

    "get assets" in {

      when(mockConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(assets))

      val service = new TrustServiceImpl(mockConnector)

      implicit val hc : HeaderCarrier = HeaderCarrier()

      whenReady(service.getAssets("1234567890")) {
        _ mustBe assets
      }
    }

    "get asset" in {

      val index = 0

      when(mockConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(assets))

      val service = new TrustServiceImpl(mockConnector)

      implicit val hc : HeaderCarrier = HeaderCarrier()

      whenReady(service.getBusinessAsset("1234567890", index)) {
        _ mustBe businessAsset
      }

      whenReady(service.getPartnershipAsset("1234567890", index)) {
        _ mustBe partnershipAsset
      }

      whenReady(service.getPropertyOrLandAsset("1234567890", index)) {
        _ mustBe propertyOrLandAsset
      }

      whenReady(service.getSharesAsset("1234567890", index)) {
        _ mustBe sharesAsset
      }

      whenReady(service.getMonetaryAsset("1234567890", index)) {
        _ mustBe moneyAsset
      }

      whenReady(service.getOtherAsset("1234567890", index)) {
        _ mustBe otherAsset
      }

      whenReady(service.getNonEeaBusinessAsset("1234567890", index)) {
        _ mustBe nonEeaBusinessAsset
      }
    }

    "remove asset" in {

      when(mockConnector.removeAsset(any(),any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val service = new TrustServiceImpl(mockConnector)

      val asset : RemoveAsset =  RemoveAsset(
        index = 0,
        endDate = LocalDate.now()
      )

      implicit val hc : HeaderCarrier = HeaderCarrier()

      whenReady(service.removeAsset("1234567890", asset)) { r =>
        r.status mustBe 200
      }
    }

  }

}
