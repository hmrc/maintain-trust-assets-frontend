/*
 * Copyright 2022 HM Revenue & Customs
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
import config.annotations.Partnership
import controllers.IndexValidation
import forms.StartDateFormProvider
import models.NormalMode
import navigation.Navigator
import pages.asset.partnership.PartnershipStartDatePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import views.html.asset.partnership.PartnershipStartDateView

import java.time.{LocalDate, ZoneOffset}

class PartnershipStartDateControllerSpec extends SpecBase with IndexValidation {

  private val prefix: String = "partnership.startDate"
  private val validAnswer = LocalDate.now(ZoneOffset.UTC)

  private val formProvider = new StartDateFormProvider(frontendAppConfig)
  private val form = formProvider.withConfig(prefix)

  private lazy val partnershipStartDateRoute = routes.PartnershipStartDateController.onPageLoad(NormalMode).url

  "PartnershipStartDate Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, partnershipStartDateRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[PartnershipStartDateView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(PartnershipStartDatePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, partnershipStartDateRoute)

      val view = application.injector.instanceOf[PartnershipStartDateView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].qualifiedWith(classOf[Partnership]).toInstance(fakeNavigator))
          .build()

      val request =
        FakeRequest(POST, partnershipStartDateRoute)
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

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, partnershipStartDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[PartnershipStartDateView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, partnershipStartDateRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, partnershipStartDateRoute)
          .withFormUrlEncodedBody(
            "value.day" -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year" -> validAnswer.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

}
