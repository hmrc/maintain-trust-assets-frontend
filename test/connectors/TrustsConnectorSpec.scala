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

import java.time.LocalDate

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.assets.{AssetMonetaryAmount, Assets, BusinessAssetType, NonEeaBusinessType, OtherAssetType, PartnershipType, PropertyLandType, SharesType}
import models.{NonUkAddress, RemoveAsset, TrustDetails, TypeOfTrust}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Inside}
import play.api.libs.json.{JsBoolean, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

class TrustsConnectorSpec extends SpecBase with Generators with ScalaFutures
  with Inside with BeforeAndAfterAll with BeforeAndAfterEach with IntegrationPatience {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  protected val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }

  val identifier = "1000000008"
  val index = 0
  val description = "description"
  val date: LocalDate = LocalDate.parse("2019-02-03")

  private val trustsUrl: String = "/trusts"
  private val assetsUrl: String = s"$trustsUrl/assets"

  private def getTrustDetailsUrl(identifier: String) = s"$trustsUrl/$identifier/trust-details"
  private def isTrust5mldUrl(identifier: String) = s"$trustsUrl/$identifier/is-trust-5mld"
  private def getAssetsUrl(identifier: String) = s"$assetsUrl/$identifier/transformed"

  private def addMoneyAssetUrl(identifier: String) = s"$assetsUrl/add-money/$identifier"
  private def amendMoneyAssetUrl(identifier: String, index: Int) = s"$assetsUrl/amend-money/$identifier/$index"
  private def addPropertyOrLandAssetUrl(identifier: String) = s"$assetsUrl/add-property-or-land/$identifier"
  private def amendPropertyOrLandAssetUrl(identifier: String, index: Int) = s"$assetsUrl/amend-property-or-land/$identifier/$index"
  private def addSharesAssetUrl(identifier: String) = s"$assetsUrl/add-shares/$identifier"
  private def amendSharesAssetUrl(identifier: String, index: Int) = s"$assetsUrl/amend-shares/$identifier/$index"
  private def addBusinessAssetUrl(identifier: String) = s"$assetsUrl/add-business/$identifier"
  private def amendPartnershipAssetUrl(identifier: String, index: Int) = s"$assetsUrl/amend-partnership/$identifier/$index"
  private def addPartnershipAssetUrl(identifier: String) = s"$assetsUrl/add-partnership/$identifier"
  private def amendOtherAssetUrl(identifier: String, index: Int) = s"$assetsUrl/amend-other/$identifier/$index"
  private def addOtherAssetUrl(identifier: String) = s"$assetsUrl/add-other/$identifier"
  private def amendBusinessAssetUrl(identifier: String, index: Int) = s"$assetsUrl/amend-business/$identifier/$index"
  private def addNonEeaBusinessAssetUrl(identifier: String) = s"$assetsUrl/add-non-eea-business/$identifier"
  private def amendNonEeaBusinessAssetUrl(identifier: String, index: Int) = s"$assetsUrl/amend-non-eea-business/$identifier/$index"
  private def removeAssetUrl(identifier: String) = s"$assetsUrl/$identifier/remove"

  val moneyAsset = AssetMonetaryAmount(123)
  val propertyOrLandAsset = PropertyLandType(None, None, 123, None)
  val sharesAsset = SharesType("", "", "", "", 123)
  val businessAsset = BusinessAssetType("", "", NonUkAddress("", "", None, ""), 123)
  val partnershipAsset = PartnershipType("", LocalDate.now)
  val otherAsset = OtherAssetType("", 123)
  val nonEeaBusinessAsset = NonEeaBusinessType(None, "orgName", NonUkAddress("", "", None, ""), "", LocalDate.now, None)

  "trust connector" when {

    "getTrustsDetails" in {

      val json = Json.parse(
        """
          |{
          | "startDate": "2019-02-03",
          | "lawCountry": "AD",
          | "administrationCountry": "GB",
          | "residentialStatus": {
          |   "uk": {
          |     "scottishLaw": false,
          |     "preOffShore": "AD"
          |   }
          | },
          | "typeOfTrust": "Will Trust or Intestacy Trust",
          | "deedOfVariation": "Previously there was only an absolute interest under the will",
          | "interVivos": false
          |}
          |""".stripMargin)

      val application = applicationBuilder()
        .configure(
          Seq(
            "microservice.services.trusts.port" -> server.port(),
            "auditing.enabled" -> false
          ): _*
        ).build()

      val connector = application.injector.instanceOf[TrustsConnector]

      server.stubFor(
        get(urlEqualTo(getTrustDetailsUrl(identifier)))
          .willReturn(okJson(json.toString))
      )

      val processed = connector.getTrustDetails(identifier)

      whenReady(processed) {
        r =>
          r mustBe TrustDetails(startDate = date, typeOfTrust = Some(TypeOfTrust.WillTrustOrIntestacyTrust), trustTaxable = None)
      }

    }

    "isTrust5mld" must {

      "return true" when {
        "untransformed data is 5mld" in {

          val json = JsBoolean(true)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled" -> false
              ): _*
            ).build()

          val connector = application.injector.instanceOf[TrustsConnector]

          server.stubFor(
            get(urlEqualTo(isTrust5mldUrl(identifier)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.isTrust5mld(identifier)

          whenReady(processed) {
            r =>
              r mustBe true
          }
        }
      }

      "return false" when {
        "untransformed data is 4mld" in {

          val json = JsBoolean(false)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled" -> false
              ): _*
            ).build()

          val connector = application.injector.instanceOf[TrustsConnector]

          server.stubFor(
            get(urlEqualTo(isTrust5mldUrl(identifier)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.isTrust5mld(identifier)

          whenReady(processed) {
            r =>
              r mustBe false
          }
        }
      }
    }

    "getAssets" when {

      "there are no assets" must {

        "return a default empty list of assets" in {

          val json = Json.parse(
            """
              |{
              | "assets": {
              | }
              |}
              |""".stripMargin)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled" -> false
              ): _*
            ).build()

          val connector = application.injector.instanceOf[TrustsConnector]

          server.stubFor(
            get(urlEqualTo(getAssetsUrl(identifier)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.getAssets(identifier)

          whenReady(processed) {
            result =>
              result mustBe Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil)
          }

          application.stop()
        }
      }

      "there are assets" must {

        "parse the response and return the assets" in {

          val json = Json.parse(
            """
              | {
              |    "assets": {
              |        "monetary": [
              |          {
              |            "assetMonetaryAmount": 1000,
              |            "startDate": "2019-02-03"
              |          }
              |        ]
              |     }
              | }
              |""".stripMargin)

          val application = applicationBuilder()
            .configure(
              Seq(
                "microservice.services.trusts.port" -> server.port(),
                "auditing.enabled" -> false
              ): _*
            ).build()

          val connector = application.injector.instanceOf[TrustsConnector]

          server.stubFor(
            get(urlEqualTo(getAssetsUrl(identifier)))
              .willReturn(okJson(json.toString))
          )

          val processed = connector.getAssets(identifier)

          whenReady(processed) {
            result =>
              result mustBe Assets(monetary = List(AssetMonetaryAmount(1000)),
                propertyOrLand = Nil,
                shares = Nil,
                business = Nil,
                partnerShip = Nil,
                other = Nil,
                nonEEABusiness = Nil)
          }

          application.stop()
        }
      }
    }

    "addMoneyAsset" must {
      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addMoneyAssetUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.addMoneyAsset(identifier, moneyAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addMoneyAssetUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.addMoneyAsset(identifier, moneyAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }
    "amendMoneyAsset" must {

      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendMoneyAssetUrl(identifier, index)))
            .willReturn(ok)
        )

        val result = connector.amendMoneyAsset(identifier, index, moneyAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendMoneyAssetUrl(identifier, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendMoneyAsset(identifier, index, moneyAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }

    "addPropertyOrLandAsset" must {
      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addPropertyOrLandAssetUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.addPropertyOrLandAsset(identifier, propertyOrLandAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addPropertyOrLandAssetUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.addPropertyOrLandAsset(identifier, propertyOrLandAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }
    "amendPropertyOrLandAsset" must {

      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendPropertyOrLandAssetUrl(identifier, index)))
            .willReturn(ok)
        )

        val result = connector.amendPropertyOrLandAsset(identifier, index, propertyOrLandAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendPropertyOrLandAssetUrl(identifier, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendPropertyOrLandAsset(identifier, index, propertyOrLandAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }

    "addSharesAsset" must {
      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addSharesAssetUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.addSharesAsset(identifier, sharesAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addSharesAssetUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.addSharesAsset(identifier, sharesAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }
    "amendSharesAsset" must {

      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendSharesAssetUrl(identifier, index)))
            .willReturn(ok)
        )

        val result = connector.amendSharesAsset(identifier, index, sharesAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendSharesAssetUrl(identifier, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendSharesAsset(identifier, index, sharesAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }

    "addBusinessAsset" must {
      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addBusinessAssetUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.addBusinessAsset(identifier, businessAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addBusinessAssetUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.addBusinessAsset(identifier, businessAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }
    "amendBusinessAsset" must {

      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendBusinessAssetUrl(identifier, index)))
            .willReturn(ok)
        )

        val result = connector.amendBusinessAsset(identifier, index, businessAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendBusinessAssetUrl(identifier, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendBusinessAsset(identifier, index, businessAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }

    "addPartnershipAsset" must {
      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addPartnershipAssetUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.addPartnershipAsset(identifier, partnershipAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addPartnershipAssetUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.addPartnershipAsset(identifier, partnershipAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }
    "amendPartnershipAsset" must {

      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendPartnershipAssetUrl(identifier, index)))
            .willReturn(ok)
        )

        val result = connector.amendPartnershipAsset(identifier, index, partnershipAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendPartnershipAssetUrl(identifier, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendPartnershipAsset(identifier, index, partnershipAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }

    "addOtherAsset" must {
      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addOtherAssetUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.addOtherAsset(identifier, otherAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addOtherAssetUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.addOtherAsset(identifier, otherAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }
    "amendOtherAsset" must {

      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendOtherAssetUrl(identifier, index)))
            .willReturn(ok)
        )

        val result = connector.amendOtherAsset(identifier, index, otherAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendOtherAssetUrl(identifier, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendOtherAsset(identifier, index, otherAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }

    "addNonEeaBusinessAsset" must {
      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addNonEeaBusinessAssetUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.addNonEeaBusinessAsset(identifier, nonEeaBusinessAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(addNonEeaBusinessAssetUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.addNonEeaBusinessAsset(identifier, nonEeaBusinessAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }
    "amendNonEeaBusinessAsset" must {

      "Return OK when the request is successful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendNonEeaBusinessAssetUrl(identifier, index)))
            .willReturn(ok)
        )

        val result = connector.amendNonEeaBusinessAsset(identifier, index, nonEeaBusinessAsset)
        result.futureValue.status mustBe OK
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {
        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          post(urlEqualTo(amendNonEeaBusinessAssetUrl(identifier, index)))
            .willReturn(badRequest)
        )

        val result = connector.amendNonEeaBusinessAsset(identifier, index, nonEeaBusinessAsset)
        result.map(response => response.status mustBe BAD_REQUEST)
        application.stop()
      }
    }

    "removeTrustee" must {

      "return Ok when the request is successful" in {

        val identifier = "1000000008"

        val trustee = RemoveAsset(
          index = 0,
          endDate = LocalDate.now()
        )

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          put(urlEqualTo(removeAssetUrl(identifier)))
            .willReturn(ok)
        )

        val result = connector.removeAsset(identifier, trustee)

        result.futureValue.status mustBe (OK)

        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val identifier = "1000000008"

        val trustee = RemoveAsset(
          index = 0,
          endDate = LocalDate.now()
        )

        val application = applicationBuilder()
          .configure(
            Seq(
              "microservice.services.trusts.port" -> server.port(),
              "auditing.enabled" -> false
            ): _*
          ).build()

        val connector = application.injector.instanceOf[TrustsConnector]

        server.stubFor(
          put(urlEqualTo(removeAssetUrl(identifier)))
            .willReturn(badRequest)
        )

        val result = connector.removeAsset(identifier, trustee)

        result.map(response => response.status mustBe BAD_REQUEST)

        application.stop()
      }

    }
  }
}
