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

package controllers.asset.partnership

import java.time.{LocalDate, ZoneOffset}
import base.SpecBase
import connectors.TrustsConnector
import models.Status.Completed
import models.WhatKindOfAsset.Partnership
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.partnership._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.PartnershipPrintHelper
import views.html.asset.partnership.PartnershipAnswersView

import scala.concurrent.Future

class PartnershipAnswerControllerSpec extends SpecBase {

  val validDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
  val name: String = "Description"

  lazy val partnershipAnswerRoute: String = add.routes.PartnershipAnswerController.onPageLoad().url

  "PartnershipAnswer Controller" must {

    val answers =
      emptyUserAnswers
        .set(WhatKindOfAssetPage, Partnership).success.value
        .set(PartnershipDescriptionPage, "Partnership Description").success.value
        .set(PartnershipStartDatePage, validDate).success.value
        .set(AssetStatus, Completed).success.value

    "on GET" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, partnershipAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnershipAnswersView]
        val printHelper = application.injector.instanceOf[PartnershipPrintHelper]
        val answerSection = printHelper(answers, provisional = true, name)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(answerSection)(request, messages).toString

        application.stop()
      }

      "redirect to Session Expired if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(GET, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

    }

    "on POST" must {

      "redirect to the next page when valid data is submitted" in {

        val mockTrustConnector = mock[TrustsConnector]

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
          .build()

        when(mockTrustConnector.addPartnershipAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        val request = FakeRequest(POST, add.routes.PartnershipAnswerController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

        application.stop()
      }

      "redirect to Session Expired if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(POST, add.routes.PartnershipAnswerController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

    }
  }
}
