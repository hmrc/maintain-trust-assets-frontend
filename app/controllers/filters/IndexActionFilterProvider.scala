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

package controllers.filters

import com.google.inject.Inject
import handlers.ErrorHandler
import models.requests.RegistrationDataRequest
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.Reads
import play.api.mvc.{ActionFilter, Result}
import queries.Gettable

import scala.concurrent.{ExecutionContext, Future}

class IndexActionFilter[T](index : Int, entity : Gettable[List[T]], errorHandler : ErrorHandler)
                          (implicit val reads : Reads[T], val executionContext: ExecutionContext)
  extends ActionFilter[RegistrationDataRequest] {

  private val logger: Logger = Logger(getClass)

  override protected def filter[A](request: RegistrationDataRequest[A]): Future[Option[Result]] = {

    lazy val entities = request.userAnswers.get(entity).getOrElse(List.empty)

    logger.info(s"[Session ID: ${request.sessionId}] Validating index on ${entity.path} for entities ${entities.size}")

    if (index >= 0 && index <= entities.size) {
      Future.successful(None)
    } else {
      logger.info(s"[Session ID: ${request.sessionId}] Out of bounds index for entity ${entity.path} index $index")
      errorHandler.onClientError(request, Status.NOT_FOUND).map(Some(_))
    }
  }
}

class IndexActionFilterProvider @Inject()(executionContext: ExecutionContext,
                                          errorHandler: ErrorHandler)
{
  def apply[T](index: Int, entity : Gettable[List[T]])(implicit reads: Reads[T]) =
    new IndexActionFilter[T](index, entity, errorHandler)(reads, executionContext)
}