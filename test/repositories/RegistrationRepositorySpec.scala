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

import base.SpecBase
import connectors.SubmissionDraftConnector
import models.Status.InProgress
import models._
import org.mockito.Matchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.MustMatchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.LocalDateTime
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RegistrationRepositorySpec extends SpecBase with MustMatchers with MockitoSugar {

  private val mockSubmissionSetFactory: SubmissionSetFactory = mock[SubmissionSetFactory]

  private def createRepository(connector: SubmissionDraftConnector,submissionSetFactory: SubmissionSetFactory): DefaultRegistrationsRepository = {
    new DefaultRegistrationsRepository(connector, frontendAppConfig, submissionSetFactory)
  }

  "RegistrationRepository" when {

    "getting answers" must {

      "read answers from my section" in {

        implicit lazy val hc: HeaderCarrier = HeaderCarrier()

        val userAnswers = UserAnswers(draftId = fakeDraftId, internalAuthId = "internalId")

        val mockConnector = mock[SubmissionDraftConnector]

        val repository = createRepository(mockConnector, mockSubmissionSetFactory)

        val response = SubmissionDraftResponse(LocalDateTime.now, Json.toJson(userAnswers), None)

        when(mockConnector.getDraftSection(any(), any())(any(), any())).thenReturn(Future.successful(response))

        val result = Await.result(repository.get(fakeDraftId), Duration.Inf)

        result mustBe Some(userAnswers)
        verify(mockConnector).getDraftSection(fakeDraftId, frontendAppConfig.repositoryKey)
      }
    }

    "setting answers" must {

      "send all relevant information as a set" in {

        implicit lazy val hc: HeaderCarrier = HeaderCarrier()

        val submissionSet = RegistrationSubmission.DataSet(
          Json.obj(),
          Some(InProgress),
          Nil,
          Nil
        )

        val mockConnector = mock[SubmissionDraftConnector]

        val mockSubmissionSetFactory = mock[SubmissionSetFactory]
        when(mockSubmissionSetFactory.createFrom(any())(any())).thenReturn(submissionSet)

        val repository = createRepository(mockConnector, mockSubmissionSetFactory)

        when(mockConnector.setDraftSectionSet(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK)))

        val userAnswers = UserAnswers(draftId = fakeDraftId, internalAuthId = "internalId")

        val result = Await.result(repository.set(userAnswers), Duration.Inf)

        result mustBe true
        verify(mockConnector).setDraftSectionSet(fakeDraftId, frontendAppConfig.repositoryKey, submissionSet)
      }
    }
   }
 }
