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
import models.UserAnswers
import pages.RegistrationProgress
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AssetsRepository @Inject()(
                                  registrationsRepository: RegistrationsRepository,
                                  registrationProgress: RegistrationProgress)
                                (implicit executionContext: ExecutionContext) {

  def get(draftId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    registrationsRepository.get(draftId)

  def set(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean] = {
    registrationsRepository.setRegistrationSectionSet(
      userAnswers,
      "assets",
      registrationProgress.assetsStatus(userAnswers))
  }
}
