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
import config.annotations.NonEeaBusiness
import controllers.IndexValidation
import forms.StartDateFormProvider
import models.UserAnswers
import navigation.Navigator
import pages.asset.noneeabusiness.NamePage
import pages.asset.noneeabusiness.add.StartDatePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import views.html.asset.noneeabusiness.add.StartDateView

class StartDateControllerSpec extends SpecBase with IndexValidation {

  private val name = "Test"

  private val trustStartDate: LocalDate = LocalDate.parse("1996-02-03")
  private val validAnswer: LocalDate = LocalDate.parse("1996-02-03")
  private val toEarlyDate: LocalDate = LocalDate.parse("1996-02-02")

  private val formProvider = new StartDateFormProvider(frontendAppConfig)
  private val prefix: String = "nonEeaBusiness.startDate"
  private val form = formProvider.withConfig(prefix, trustStartDate)

  private lazy val onPageLoadRoute = routes.StartDateController.onPageLoad().url

  private val baseAnswers: UserAnswers = emptyUserAnswers.copy(whenTrustSetup = trustStartDate)
    .set(NamePage, name).success.value


  "StartDateController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[StartDateView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, name)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers
        .set(StartDatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val view = application.injector.instanceOf[StartDateView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), name)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(bind[Navigator].qualifiedWith(classOf[NonEeaBusiness]).toInstance(fakeNavigator))
        .build()

      val request =
        FakeRequest(POST, onPageLoadRoute)
          .withFormUrlEncodedBody(
            "value.day" -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year" -> validAnswer.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when the entered date is before the trust start date" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, onPageLoadRoute)
          .withFormUrlEncodedBody(
            "value.day" -> toEarlyDate.getDayOfMonth.toString,
            "value.month" -> toEarlyDate.getMonthValue.toString,
            "value.year" -> toEarlyDate.getYear.toString
          )

      val boundForm = form.bind(Map(
        "value.day" -> toEarlyDate.getDayOfMonth.toString,
        "value.month" -> toEarlyDate.getMonthValue.toString,
        "value.year" -> toEarlyDate.getYear.toString
      ))

      val view = application.injector.instanceOf[StartDateView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, name)(request, messages).toString

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, onPageLoadRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[StartDateView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, name)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, onPageLoadRoute)
          .withFormUrlEncodedBody(
            "value.day" -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year" -> validAnswer.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }

}
