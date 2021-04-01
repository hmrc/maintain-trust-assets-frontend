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

package controllers.asset.noneeabusiness.add

import java.time.LocalDate

import base.SpecBase
import connectors.TrustsConnector
import controllers.asset.noneeabusiness.routes
import controllers.routes._
import models.{InternationalAddress, UserAnswers}
import pages.asset.noneeabusiness._
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.print.NonEeaBusinessPrintHelper
import views.html.asset.noneeabusiness.add.AnswersView
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class AnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val name: String = "Noneeabusiness"

  private val answers: UserAnswers = emptyUserAnswers
    .set(NamePage, name).success.value
    .set(InternationalAddressPage, InternationalAddress("Line 1", "Line 2", Some("Line 3"), "FR")).success.value
    .set(GoverningCountryPage, "FR").success.value
    .set(StartDatePage, LocalDate.parse("1996-02-03")).success.value

  private lazy val onPageLoadRoute: String = routes.AnswersController.onSubmit().url

  "AnswersController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AnswersView]

      val printHelper = application.injector.instanceOf[NonEeaBusinessPrintHelper]
      val answerSection = printHelper(answers, provisional = true, name)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answerSection)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {
      val mockTrustsConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustsConnector))
        .build()

      when(mockTrustsConnector.addNonEeaBusinessAsset(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, routes.AnswersController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, routes.AnswersController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

  }
}
