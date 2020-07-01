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
import models.UserAnswers
import play.api.http
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class DefaultRegistrationsRepository @Inject()(submissionDraftConnector: SubmissionDraftConnector,
                                               config: FrontendAppConfig,
                                               submissionSetFactory: SubmissionSetFactory
                                        )(implicit ec: ExecutionContext) extends RegistrationsRepository {

  private val userAnswersSection = config.appName

  override def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean] = {
    submissionDraftConnector.setDraftSectionSet(
      userAnswers.draftId,
      userAnswersSection,
      submissionSetFactory.createFrom(userAnswers)
    ).map {
      response => response.status == http.Status.OK
    }
  }

  override def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    submissionDraftConnector.getDraftSection(draftId, userAnswersSection).map {
      response =>
        response.data.validate[UserAnswers] match {
          case JsSuccess(userAnswers, _) => Some(userAnswers)
          case _ => None
        }
    }
  }
}

trait RegistrationsRepository {

  def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean]

  def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]]
}
