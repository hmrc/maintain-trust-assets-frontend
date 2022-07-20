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

package controllers.asset.property_or_land

import base.SpecBase
import config.annotations.PropertyOrLand
import controllers.IndexValidation
import controllers.routes._
import forms.UKAddressFormProvider
import generators.ModelGenerators
import models.{NormalMode, UkAddress}
import navigation.Navigator
import pages.asset.property_or_land.PropertyOrLandUKAddressPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import views.html.asset.property_or_land.PropertyOrLandUKAddressView

class PropertyOrLandUKAddressControllerSpec extends SpecBase with ModelGenerators with IndexValidation {

  val formProvider = new UKAddressFormProvider()
  val form: Form[UkAddress] = formProvider()

  lazy val PropertyOrLandUKAddressRoute: String = routes.PropertyOrLandUKAddressController.onPageLoad(NormalMode).url

  "WhatIsThePropertyOrLandAddress Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, PropertyOrLandUKAddressRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[PropertyOrLandUKAddressView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(PropertyOrLandUKAddressPage,  UkAddress("line 1", "line 2", Some("line 3"), Some("line 4"),"line 5")).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, PropertyOrLandUKAddressRoute)

      val view = application.injector.instanceOf[PropertyOrLandUKAddressView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(UkAddress("line 1", "line 2", Some("line 3"), Some("line 4"),"line 5")), NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].qualifiedWith(classOf[PropertyOrLand]).toInstance(fakeNavigator))
          .build()

      val request =
        FakeRequest(POST, PropertyOrLandUKAddressRoute)
          .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"),("postcode", "NE1 1ZZ"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, PropertyOrLandUKAddressRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[PropertyOrLandUKAddressView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, PropertyOrLandUKAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, PropertyOrLandUKAddressRoute)
          .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"),("postcode", "NE1 1ZZ"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

}
