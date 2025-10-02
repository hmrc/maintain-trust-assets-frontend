/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase
import connectors.TrustsConnector
import models.WhatKindOfAsset.Partnership
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.asset.WhatKindOfAssetPage
import pages.asset.partnership._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.PartnershipPrintHelper
import views.html.asset.partnership.PartnershipAnswersView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class PartnershipAnswerControllerSpec extends SpecBase {

  val validDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
  val name: String = "Description"

  private val partnershipAnswerRoute = "/maintain-a-trust/trust-assets/partnership/0/partnership-check-answers"

  "PartnershipAnswer Controller" must {

    val answers =
      emptyUserAnswers
        .set(WhatKindOfAssetPage(index), Partnership).success.value
        .set(PartnershipDescriptionPage(index), "Partnership Description").success.value
        .set(PartnershipStartDatePage(index), validDate).success.value

    "on GET" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, partnershipAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnershipAnswersView]
        val printHelper = application.injector.instanceOf[PartnershipPrintHelper]
        val answerSection = printHelper(answers, index, provisional = true, name)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, answerSection)(request, messages).toString

        application.stop()
      }

      "redirect to Session Expired if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(GET, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }
    }

    "on POST" must {

      "redirect to the next page when valid data is submitted" in {
        val mockTrustConnector = mock[TrustsConnector]

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
          .build()

        when(mockTrustConnector.getAssets(any())(any(), any()))
          .thenReturn(Future.successful(models.assets.Assets()))

        when(mockTrustConnector.amendPartnershipAsset(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        when(mockTrustConnector.addPartnershipAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        val request = FakeRequest(POST, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

        application.stop()
      }

      "redirect to Session Expired if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(POST, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }
    }
  }
}
