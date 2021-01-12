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

package controllers.actions

import models.{Status, UserAnswers}
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import repositories.RegistrationsRepository

import scala.concurrent.{ExecutionContext, Future}

class FakeDraftIdRetrievalActionProvider(draftId: String,
                                         status :  Status = Status.InProgress,
                                         dataToReturn : Option[UserAnswers],
                                         registrationsRepository : RegistrationsRepository)
  extends DraftIdRetrievalActionProvider with MockitoSugar {

  implicit val executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val mockedRepository: RegistrationsRepository = mock[RegistrationsRepository]

  when(mockedRepository.get(any())(any())).thenReturn(Future.successful(dataToReturn))

  override def apply(draftId : String) = new DraftIdDataRetrievalAction(draftId, mockedRepository, executionContext)

}




