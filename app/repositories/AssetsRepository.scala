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

import javax.inject.Inject
import mapping.AssetMapper
import models.Status.Completed
import models.{Status, SubmissionDraftRegistrationPiece, UserAnswers}
import pages.RegistrationProgress
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AssetsRepository @Inject()(
                                  registrationsRepository: RegistrationsRepository,
                                  registrationProgress: RegistrationProgress,
                                  assetMapper: AssetMapper)
                                (implicit executionContext: ExecutionContext) {

  def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    registrationsRepository.get(draftId)

  def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val status = registrationProgress.assetsStatus(userAnswers)
    val registrationPieces = mappedDataIfCompleted(userAnswers, status)

    registrationsRepository.setRegistrationSectionSet(
      userAnswers,
      "assets",
      status,
      registrationPieces)
  }

  private def mappedDataIfCompleted(userAnswers: UserAnswers, status: Option[Status]) = {
    if (status.contains(Completed)) {
      assetMapper.build(userAnswers) match {
        case Some(assets) => List(SubmissionDraftRegistrationPiece("trust/assets", Json.toJson(assets)))
        case _ => List.empty
      }
    } else {
      List.empty
    }
  }
}