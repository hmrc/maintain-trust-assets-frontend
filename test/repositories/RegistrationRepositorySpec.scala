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

package repositories

import java.time.LocalDateTime

import base.SpecBase
import config.FrontendAppConfig
import connectors.SubmissionDraftConnector
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.MustMatchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class RegistrationRepositorySpec extends PlaySpec with MustMatchers with MockitoSugar {

  private implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  "RegistrationRepository" when {

    "adding a registration section" must {

      "combine into empty sections" in {

        implicit lazy val hc: HeaderCarrier = HeaderCarrier()

        val draftId = "DraftId"

        val mockConnector = mock[SubmissionDraftConnector]

        val mockConfig = mock[FrontendAppConfig]
        when(mockConfig.appName).thenReturn("app-name")

        val repository = new DefaultRegistrationsRepository(mockConnector, mockConfig)

        val existingData = Json.parse(
          """
            |{
            |
            |}
            |""".stripMargin)
        val existingSubmissionResponse = SubmissionDraftResponse(LocalDateTime.now(), existingData, None)

        when(mockConnector.getDraftSection(any(), any())(any(), any())).thenReturn(Future.successful(existingSubmissionResponse))
        when(mockConnector.setDraftSection(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK)))

        val newData = Json.parse(
          """
            |{
            | "dataField": "newData"
            |}
            |""".stripMargin)

        val expectedRegistrationData = Json.parse(
          """
            |{
            | "field/subfield": {
            |   "dataField": "newData"
            | }
            |}
            |""".stripMargin)

        val result = Await.result(repository.setRegistrationSection(draftId, "field/subfield", newData), Duration.Inf)

        result mustBe true
        verify(mockConnector).getDraftSection(draftId, "registration")(hc, executionContext)
        verify(mockConnector).setDraftSection(draftId, "registration", expectedRegistrationData)(hc, executionContext)
      }
    }
   }
 }
