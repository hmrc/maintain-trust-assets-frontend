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

import base.SpecBase
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import generators.Generators
import models.assets._
import models.http.TaxableMigrationFlag
import models.{NonUkAddress, RemoveAsset, TrustDetails, TypeOfTrust}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.libs.json.{JsBoolean, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class TrustsConnectorSpec extends SpecBase with Generators with ScalaFutures with BeforeAndAfterAll with BeforeAndAfterEach with IntegrationPatience {

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
  val description = "description"
  val date: LocalDate = LocalDate.parse("2019-02-03")

  private val trustsUrl: String = "/trusts"
  private val assetsUrl: String = s"$trustsUrl/assets"

  private def getTrustDetailsUrl(identifier: String) = s"$trustsUrl/trust-details/$identifier/transformed"

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

  private def getTrustMigrationFlagUrl(identifier: String) = s"/trusts/$identifier/taxable-migration/migrating-to-taxable"

  val moneyAsset: AssetMonetaryAmount = AssetMonetaryAmount(123)
  val propertyOrLandAsset: PropertyLandType = PropertyLandType(None, None, 123, None)
  val sharesAsset: SharesType = SharesType("", "", "", "", 123)
  val businessAsset: BusinessAssetType = BusinessAssetType("", "", NonUkAddress("", "", None, ""), 123)
  val partnershipAsset: PartnershipType = PartnershipType("", LocalDate.now)
  val otherAsset: OtherAssetType = OtherAssetType("", 123)
  val nonEeaBusinessAsset: NonEeaBusinessType = NonEeaBusinessType(None, "orgName", NonUkAddress("", "", None, ""), "", LocalDate.now, None, provisional = true)

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

        val processed = connector.addMoneyAsset(index, identifier, moneyAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.addMoneyAsset(index, identifier, moneyAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }

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

        val processed = connector.amendMoneyAsset(identifier, index, moneyAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.amendMoneyAsset(identifier, index, moneyAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.addPropertyOrLandAsset(index, identifier, propertyOrLandAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.addPropertyOrLandAsset(index, identifier, propertyOrLandAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.amendPropertyOrLandAsset(identifier, index, propertyOrLandAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.amendPropertyOrLandAsset(identifier, index, propertyOrLandAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.addSharesAsset(index, identifier, sharesAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.addSharesAsset(index, identifier, sharesAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.amendSharesAsset(identifier, index, sharesAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.amendSharesAsset(identifier, index, sharesAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.addBusinessAsset(index, identifier, businessAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.addBusinessAsset(index, identifier, businessAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.amendBusinessAsset(identifier, index, businessAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.amendBusinessAsset(identifier, index, businessAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.addPartnershipAsset(index, identifier, partnershipAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.addPartnershipAsset(index, identifier, partnershipAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.amendPartnershipAsset(identifier, index, partnershipAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.amendPartnershipAsset(identifier, index, partnershipAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.addOtherAsset(index, identifier, otherAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.addOtherAsset(index, identifier, otherAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.amendOtherAsset(identifier, index, otherAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.amendOtherAsset(identifier, index, otherAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.addNonEeaBusinessAsset(index, identifier, nonEeaBusinessAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.addNonEeaBusinessAsset(index, identifier, nonEeaBusinessAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
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

        val processed = connector.amendNonEeaBusinessAsset(identifier, index, nonEeaBusinessAsset)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
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

        val processed = connector.amendNonEeaBusinessAsset(identifier, index, nonEeaBusinessAsset)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
        application.stop()
      }
    }

    "removeTrustee" must {

      "return Ok when the request is successful" in {

        val identifier = "1000000008"

        val trustee = RemoveAsset(
          `type` = AssetNameType.NonEeaBusinessAssetNameType,
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

        val processed = connector.removeAsset(identifier, trustee)

        whenReady(processed) {
          r =>
            r.status mustBe OK
        }
        application.stop()
      }

      "return Bad Request when the request is unsuccessful" in {

        val identifier = "1000000008"

        val trustee = RemoveAsset(
          `type` = AssetNameType.NonEeaBusinessAssetNameType,
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

        val processed = connector.removeAsset(identifier, trustee)

        whenReady(processed) {
          r =>
            r.status mustBe BAD_REQUEST
        }
        application.stop()
      }

    }
    "getTrustMigrationFlag" when {

      "value defined" in {

        val json = Json.parse(
          """
            |{
            | "value": true
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
          get(urlEqualTo(getTrustMigrationFlagUrl(identifier)))
            .willReturn(okJson(json.toString))
        )

        val result = connector.getTrustMigrationFlag(identifier)

        whenReady(result) { r =>
          r mustBe TaxableMigrationFlag(Some(true))
        }

        application.stop()
      }

      "value undefined" in {

        val json = Json.parse(
          """
            |{}
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
          get(urlEqualTo(getTrustMigrationFlagUrl(identifier)))
            .willReturn(okJson(json.toString))
        )

        val result = connector.getTrustMigrationFlag(identifier)

        whenReady(result) { r =>
          r mustBe TaxableMigrationFlag(None)
        }

        application.stop()
      }
    }

  }
}
