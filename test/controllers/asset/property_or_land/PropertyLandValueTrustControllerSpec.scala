/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.asset.property_or_land

import base.SpecBase
import config.annotations.PropertyOrLand
import controllers.IndexValidation
import controllers.routes._
import forms.ValueFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import pages.asset.property_or_land.{PropertyLandValueTrustPage, PropertyOrLandTotalValuePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.asset.property_or_land.PropertyLandValueTrustView

class PropertyLandValueTrustControllerSpec extends SpecBase with IndexValidation {

  val formProvider     = new ValueFormProvider(frontendAppConfig)
  val form: Form[Long] = formProvider.withConfig(prefix = "propertyOrLand.valueInTrust")

  val validAnswer: Long = 4000L

  lazy val propertyLandValueTrustRoute: String =
    routes.PropertyLandValueTrustController.onPageLoad(index, NormalMode).url

  val baseAnswers: UserAnswers = emptyUserAnswers.set(PropertyOrLandTotalValuePage(index), 5000L).success.value

  "PropertyLandValueTrust Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, propertyLandValueTrustRoute)

      val view = application.injector.instanceOf[PropertyLandValueTrustView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, index, NormalMode)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(PropertyLandValueTrustPage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, propertyLandValueTrustRoute)

      val view = application.injector.instanceOf[PropertyLandValueTrustView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), index, NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(bind[Navigator].qualifiedWith(classOf[PropertyOrLand]).toInstance(fakeNavigator))
        .build()

      val request = FakeRequest(POST, propertyLandValueTrustRoute)
        .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(POST, propertyLandValueTrustRoute)
        .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[PropertyLandValueTrustView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, propertyLandValueTrustRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, propertyLandValueTrustRoute)
        .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

  }

}
