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

package controllers.asset.business.add

import base.SpecBase
import connectors.TrustsConnector
import controllers.routes._
import models.{UkAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.asset.business._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.BusinessPrintHelper
import views.html.asset.business.add.BusinessAnswersView

import scala.concurrent.Future

class BusinessAnswersControllerSpec extends SpecBase {

  val index = 0

  private val name: String = "Business"

  val answers: UserAnswers = emptyUserAnswers
    .set(BusinessNamePage, name).success.value
    .set(BusinessDescriptionPage, "test test test").success.value
    .set(BusinessAddressUkYesNoPage, true).success.value
    .set(BusinessUkAddressPage, UkAddress("test", "test", None, None, "NE11NE")).success.value
    .set(BusinessValuePage, 12L).success.value

  "BusinessAnswersController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, routes.BusinessAnswersController.onPageLoad().url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BusinessAnswersView]
      val printHelper = application.injector.instanceOf[BusinessPrintHelper]
      val answerSection = printHelper(answers, provisional = true, name)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answerSection)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {
      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
        .build()

      when(mockTrustConnector.addBusinessAsset(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.BusinessAnswersController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

  }
}
