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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import models.Status.InProgress
import models.{RegistrationSubmission, SubmissionDraftResponse}
import org.scalatest.{MustMatchers, OptionValues}
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.CONTENT_TYPE
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

import java.time.LocalDateTime
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
  private val setSubmissionUrl = s"$submissionsUrl/$testDraftId/set/$testSection"

  "SubmissionDraftConnector" when {

    "submission drafts" must {

      "set data for section set" in {

        val sectionData = Json.parse(
          """
            |{
            | "field1": "value1",
            | "field2": "value2"
            |}
            |""".stripMargin)

        val submissionDraftSetData = RegistrationSubmission.DataSet(
          sectionData,
          Some(InProgress),
          Nil,
          Nil
        )

        server.stubFor(
          post(urlEqualTo(setSubmissionUrl))
            .withHeader(CONTENT_TYPE, containing("application/json"))
            .withRequestBody(equalTo(Json.toJson(submissionDraftSetData).toString()))
            .willReturn(
              aResponse()
                .withStatus(Status.OK)
            )
        )

        val result = Await.result(connector.setDraftSectionSet(testDraftId, testSection, submissionDraftSetData), Duration.Inf)
        result.status mustBe Status.OK
      }

      "retrieve data for section" in {

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
    }
  }
}
