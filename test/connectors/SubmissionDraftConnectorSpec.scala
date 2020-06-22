/*
 * Copyright 2020 HM Revenue & Customs
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

import java.time.LocalDateTime

import base.SpecBase
import models.{SubmissionDraftData, SubmissionDraftId, SubmissionDraftResponse}
import org.scalatest.{FreeSpec, MustMatchers, OptionValues}
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.CONTENT_TYPE
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper
import com.github.tomakehurst.wiremock.client.WireMock._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SubmissionDraftConnectorSpec extends SpecBase with MustMatchers with OptionValues with WireMockHelper {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Seq(
      "microservice.services.trusts.port" -> server.port(),
      "auditing.enabled" -> false): _*
    ).build()

  private lazy val connector = injector.instanceOf[SubmissionDraftConnector]

  private val testDraftId = "draftId"
  private val testSection = "section"
  private val submissionsUrl = s"/trusts/register/submission-drafts"
  private val submissionUrl = s"$submissionsUrl/$testDraftId/$testSection"
  private val mainUrl = s"$submissionsUrl/$testDraftId/MAIN"
  private val deleteUrl = s"$submissionsUrl/$testDraftId"

  "SubmissionDraftConnector" when {

    "submission drafts" must {

      "be set for main" in {

        val sectionData = Json.parse(
          """
            |{
            | "field1": "value1",
            | "field2": "value2"
            |}
            |""".stripMargin)

        val submissionDraftData = SubmissionDraftData(sectionData, Some("ref"), Some(true))

        server.stubFor(
          post(urlEqualTo(mainUrl))
            .withHeader(CONTENT_TYPE, containing("application/json"))
            .withRequestBody(equalTo(Json.toJson(submissionDraftData).toString()))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
            )
        )

        val result = Await.result(connector.setDraftMain(testDraftId, sectionData, inProgress = true, Some("ref")), Duration.Inf)
        result.status mustBe Status.OK
      }
      "can be retrieved for main" in {

        val draftData = Json.parse(
          """
            |{
            | "field1": "value1",
            | "field2": "value2"
            |}
            |""".stripMargin)

        val draftResponseJson =
          """
            |{
            | "createdAt": "2012-02-03T09:30:00",
            | "data": {
            |  "field1": "value1",
            |  "field2": "value2"
            | }
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(mainUrl))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody(draftResponseJson)
            )
        )

        val result: SubmissionDraftResponse = Await.result(connector.getDraftMain(testDraftId), Duration.Inf)
        result.createdAt mustBe LocalDateTime.of(2012, 2, 3, 9, 30)
        result.data mustBe draftData
      }
      "can be set for section" in {

        val sectionData = Json.parse(
          """
            |{
            | "field1": "value1",
            | "field2": "value2"
            |}
            |""".stripMargin)

        val submissionDraftData = SubmissionDraftData(sectionData, None, None)

        server.stubFor(
          post(urlEqualTo(submissionUrl))
            .withHeader(CONTENT_TYPE, containing("application/json"))
            .withRequestBody(equalTo(Json.toJson(submissionDraftData).toString()))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
            )
        )

        val result = Await.result(connector.setDraftSection(testDraftId, testSection, sectionData), Duration.Inf)
        result.status mustBe Status.OK
      }
      "can be retrieved for section" in {

        val draftData = Json.parse(
          """
            |{
            | "field1": "value1",
            | "field2": "value2"
            |}
            |""".stripMargin)

        val draftResponseJson =
          """
            |{
            | "createdAt": "2012-02-03T09:30:00",
            | "data": {
            |  "field1": "value1",
            |  "field2": "value2"
            | }
            |}
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(submissionUrl))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody(draftResponseJson)
            )
        )

        val result: SubmissionDraftResponse = Await.result(connector.getDraftSection(testDraftId, testSection), Duration.Inf)
        result.createdAt mustBe LocalDateTime.of(2012, 2, 3, 9, 30)
        result.data mustBe draftData
      }
      "can have list of ids retrieved" in {

        val draftIdsResponseJson =
          """
            |[
            | {
            |   "draftId": "Draft1",
            |   "createdAt": "2012-02-03T09:30:00",
            |   "reference": "ref",
            |   "inProgress": true
            | },
            | {
            |   "draftId": "Draft2",
            |   "createdAt": "2010-06-21T14:44:00"
            | }
            |]
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(submissionsUrl))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
                .withBody(draftIdsResponseJson)
            )
        )

        val result = Await.result(connector.getCurrentDraftIds(), Duration.Inf)
        result mustBe List(
          SubmissionDraftId("Draft1", LocalDateTime.of(2012, 2, 3, 9, 30), Some("ref")),
          SubmissionDraftId("Draft2", LocalDateTime.of(2010, 6, 21, 14, 44), None)
        )
      }
      "can be deleted" in {

        server.stubFor(
          delete(urlEqualTo(deleteUrl))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
            )
        )

        val result = Await.result(connector.deleteDraft(testDraftId), Duration.Inf)
        result.status mustBe Status.OK
      }
    }
  }
}
