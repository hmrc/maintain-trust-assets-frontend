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

import config.FrontendAppConfig
import connectors.SubmissionDraftConnector
import javax.inject.Inject
import models.{Status, SubmissionDraftRegistrationPiece, SubmissionDraftSetData, SubmissionDraftStatus, UserAnswers}
import play.api.http
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class DefaultRegistrationsRepository @Inject()(submissionDraftConnector: SubmissionDraftConnector,
                                               config: FrontendAppConfig
                                        )(implicit ec: ExecutionContext) extends RegistrationsRepository {

  private val userAnswersSection = config.appName
  private val registrationSection = "registration"

  override def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    submissionDraftConnector.getDraftSection(draftId, userAnswersSection).map {
      response =>
        response.data.validate[UserAnswers] match {
          case JsSuccess(userAnswers, _) => Some(userAnswers)
          case _ => None
        }
    }
  }

  private def setSectionData(draftId: String, section: String, jsonData: JsValue)(implicit hc: HeaderCarrier) = {
    submissionDraftConnector.setDraftSection(
      draftId,
      section,
      jsonData
    ).map {
      response => response.status == http.Status.OK
    }
  }

  private def setSectionSetData(draftId: String, section: String, setData: SubmissionDraftSetData)(implicit hc: HeaderCarrier) = {
    submissionDraftConnector.setDraftSectionSet(
      draftId,
      section,
      setData
    ).map {
      response => response.status == http.Status.OK
    }
  }

  override def setRegistrationSectionSet(userAnswers: UserAnswers,
                                         statusKey: String,
                                         status: Option[Status],
                                         registrationPieces: List[SubmissionDraftRegistrationPiece])
                               (implicit hc: HeaderCarrier): Future[Boolean] = {
    val data = SubmissionDraftSetData(
      Json.toJson(userAnswers),
      Some(SubmissionDraftStatus(statusKey, status)),
      registrationPieces
    )
    setSectionSetData(userAnswers.draftId, userAnswersSection, data )
  }

  override def setRegistrationSection(draftId: String, path: String, registrationSectionData: JsValue)
                                     (implicit hc: HeaderCarrier): Future[Boolean] = {

    val sectionPath = (JsPath \ path).json

    submissionDraftConnector.getDraftSection(draftId, registrationSection).flatMap {
      data =>
        val transform = sectionPath.prune andThen __.json.update(sectionPath.put(registrationSectionData))
        data.data.transform(transform) match {
          case JsSuccess(value, _) => setSectionData(draftId, registrationSection, value)
          case _ => Future.successful(false)
        }
    }
  }
}

trait RegistrationsRepository {
  def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]]

  def setRegistrationSection(draftId: String, path: String, data: JsValue)(implicit hc: HeaderCarrier): Future[Boolean]

  def setRegistrationSectionSet(userAnswers: UserAnswers,
                                statusKey: String,
                                status: Option[Status],
                                registrationPieces: List[SubmissionDraftRegistrationPiece]
                               )(implicit hc: HeaderCarrier): Future[Boolean]
}
