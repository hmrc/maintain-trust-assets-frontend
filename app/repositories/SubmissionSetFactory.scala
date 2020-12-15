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

import mapping.AssetMapper
import models.Status.Completed
import models.{RegistrationSubmission, Status, UserAnswers}
import pages.RegistrationProgress
import play.api.libs.json.Json

import javax.inject.Inject

class SubmissionSetFactory @Inject()(registrationProgress: RegistrationProgress, assetMapper: AssetMapper) {

  def createFrom(userAnswers: UserAnswers): RegistrationSubmission.DataSet = {
    val status = registrationProgress.assetsStatus(userAnswers)

    RegistrationSubmission.DataSet(
      Json.toJson(userAnswers),
      status,
      mappedDataIfCompleted(userAnswers, status),
      Nil // TODO
    )
  }

  private def mappedDataIfCompleted(userAnswers: UserAnswers, status: Option[Status]): List[RegistrationSubmission.MappedPiece] = {
    if (status.contains(Completed)) {
      assetMapper.build(userAnswers) match {
        case Some(assets) => List(RegistrationSubmission.MappedPiece("trust/entities/beneficiary", Json.toJson(assets)))
        case _ => List.empty
      }
    } else {
      List.empty
    }
  }
}
