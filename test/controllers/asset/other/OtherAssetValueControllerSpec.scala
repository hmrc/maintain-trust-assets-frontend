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

package controllers.asset.other

import base.SpecBase
import config.annotations.{Other => other}
import forms.ValueFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages.asset.other.{OtherAssetDescriptionPage, OtherAssetValuePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.asset.other.OtherAssetValueView

class OtherAssetValueControllerSpec extends SpecBase {

  val formProvider = new ValueFormProvider(frontendAppConfig)
  val form: Form[Long] = formProvider.withConfig(prefix = "other.value")
  val description: String = "Description"
  val validAnswer: Long = 4000L

  val requiredAnswers: UserAnswers = emptyUserAnswers
    .set(OtherAssetDescriptionPage, description).success.value

  lazy val valueRoute: String = routes.OtherAssetValueController.onPageLoad(NormalMode).url

  "OtherAssetValueController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(requiredAnswers)).build()

      val request = FakeRequest(GET, valueRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[OtherAssetValueView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, description)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = requiredAnswers
        .set(OtherAssetValuePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, valueRoute)

      val view = application.injector.instanceOf[OtherAssetValueView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), NormalMode, description)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(requiredAnswers))
          .overrides(bind[Navigator].qualifiedWith(classOf[other]).toInstance(fakeNavigator))
          .build()

      val request =
        FakeRequest(POST, valueRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(requiredAnswers)).build()

      val request =
        FakeRequest(POST, valueRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[OtherAssetValueView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, description)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, valueRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, valueRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
