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
import forms.InternationalAddressFormProvider
import models.{NonUkAddress, NormalMode}
import navigation.Navigator
import pages.asset.property_or_land.PropertyOrLandInternationalAddressPage
import play.api.Application
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils._
import utils.countryOptions.CountryOptionsNonUK
import views.html.asset.property_or_land.PropertyOrLandInternationalAddressView

import scala.concurrent.Future

class PropertyOrLandInternationalAddressControllerSpec extends SpecBase with IndexValidation {

  val formProvider             = new InternationalAddressFormProvider()
  val form: Form[NonUkAddress] = formProvider()

  lazy val propertyOrLandInternationalAddressRoute: String =
    routes.PropertyOrLandInternationalAddressController.onPageLoad(index, NormalMode).url

  "PropertyOrLandInternationalAddress Controller" must {

    "return OK and the correct view for a GET" in {

      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, propertyOrLandInternationalAddressRoute)

      val view: PropertyOrLandInternationalAddressView =
        application.injector.instanceOf[PropertyOrLandInternationalAddressView]

      val result: Future[Result] = route(application, request).value

      val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptionsNonUK].options()

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, index, countryOptions, NormalMode)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers
        .set(PropertyOrLandInternationalAddressPage(index), NonUkAddress("line 1", "line 2", Some("line 3"), "country"))
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, propertyOrLandInternationalAddressRoute)

      val view = application.injector.instanceOf[PropertyOrLandInternationalAddressView]

      val result = route(application, request).value

      val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptionsNonUK].options()

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(NonUkAddress("line 1", "line 2", Some("line 3"), "country")), index, countryOptions, NormalMode)(
          request,
          messages
        ).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Navigator].qualifiedWith(classOf[PropertyOrLand]).toInstance(fakeNavigator))
          .build()

      val request =
        FakeRequest(POST, propertyOrLandInternationalAddressRoute)
          .withFormUrlEncodedBody(("line1", "value 1"), ("line2", "value 2"), ("country", "IN"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, propertyOrLandInternationalAddressRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[PropertyOrLandInternationalAddressView]

      val result = route(application, request).value

      val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptionsNonUK].options()

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, countryOptions, NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, propertyOrLandInternationalAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, propertyOrLandInternationalAddressRoute)
          .withFormUrlEncodedBody(("line 1", "value 1"), ("line 2", "value 2"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

}
