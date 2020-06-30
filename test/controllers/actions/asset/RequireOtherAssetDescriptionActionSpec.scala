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

import base.SpecBase
import models.requests.RegistrationDataRequest
import models.requests.asset.OtherAssetDescriptionRequest
import models.{Mode, NormalMode}
import org.scalatest.concurrent.ScalaFutures
import pages.asset.other.OtherAssetDescriptionPage
import play.api.mvc.Result
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolments}

import scala.concurrent.Future

class RequireOtherAssetDescriptionActionSpec extends SpecBase with ScalaFutures {

  private val mode: Mode = NormalMode
  private val index: Int = 0
  private val description: String = "Description"

  class Harness() extends RequireOtherAssetDescriptionAction(mode, index, draftId) {
    def callRefine[A](request: RegistrationDataRequest[A]): Future[Either[Result, OtherAssetDescriptionRequest[A]]] = refine(request)
  }

  "Other asset description required answer Action" when {

    "there is no answer" must {

      "redirect to Session Expired" in {

        val action = new Harness()

        val futureResult = action.callRefine(
          RegistrationDataRequest(
            fakeRequest,
            "id",
            emptyUserAnswers,
            AffinityGroup.Organisation,
            Enrolments(Set())
          )
        )

        whenReady(futureResult) { r =>
          val result = Future.successful(r.left.get)
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.WhatKindOfAssetController.onPageLoad(mode, index, draftId).url
        }
      }
    }

    "there is an answer" must {

      "add the answer to the request" in {

        val action = new Harness()

        val userAnswers = emptyUserAnswers.set(OtherAssetDescriptionPage(index), description).success.value

        val futureResult = action.callRefine(
          RegistrationDataRequest(
            fakeRequest,
            "id",
            userAnswers,
            AffinityGroup.Organisation,
            Enrolments(Set())
          )
        )

        whenReady(futureResult) { result =>
          result.right.get.description mustBe description
        }
      }
    }
  }

}
