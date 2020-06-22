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
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateFormatter

import scala.concurrent.{ExecutionContext, Future}

class DefaultRegistrationsRepository @Inject()(dateFormatter: DateFormatter,
                                               submissionDraftConnector: SubmissionDraftConnector,
                                               config: FrontendAppConfig
                                        )(implicit ec: ExecutionContext) extends RegistrationsRepository {

  override def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] = {
    submissionDraftConnector.getDraftMain(draftId).map {
      response => Some(response.data.as[UserAnswers])
    }
  }

  override def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean] = {

    submissionDraftConnector.setDraftSection(
      userAnswers.draftId,
      config.appName,
      Json.toJson(userAnswers)
    ).map {
      response => response.status == Status.OK
    }
  }
}

trait RegistrationsRepository {
  def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]]

  def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean]
}
