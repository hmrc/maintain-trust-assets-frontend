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

package controllers.actions.asset

import com.google.inject.Inject
import models.Mode
import models.requests.RegistrationDataRequest
import models.requests.asset.OtherAssetDescriptionRequest
import pages.asset.other.OtherAssetDescriptionPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}

import scala.concurrent.{ExecutionContext, Future}

class RequireOtherAssetDescriptionAction @Inject()(
                                                    mode: Mode,
                                                    index: Int,
                                                    draftId: String
                                                  )(implicit val executionContext: ExecutionContext)
  extends ActionRefiner[RegistrationDataRequest, OtherAssetDescriptionRequest] {

  override protected def refine[A](request: RegistrationDataRequest[A]): Future[Either[Result, OtherAssetDescriptionRequest[A]]] = {

    Future.successful(
      request.userAnswers.get(OtherAssetDescriptionPage(index)) match {
        case None =>
          Left(
            Redirect(controllers.routes.WhatKindOfAssetController.onPageLoad(mode, index, draftId))
          )
        case Some(description) =>
          Right(
            OtherAssetDescriptionRequest(
              request,
              description
            )
          )
      }
    )
  }
}
