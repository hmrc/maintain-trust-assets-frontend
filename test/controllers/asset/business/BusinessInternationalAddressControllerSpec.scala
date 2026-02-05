/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.asset.business

import base.SpecBase
import config.annotations.Business
import controllers.IndexValidation
import controllers.routes._
import forms.InternationalAddressFormProvider
import models.{NonUkAddress, NormalMode, UserAnswers}
import navigation.Navigator
import pages.asset.business.{BusinessInternationalAddressPage, BusinessNamePage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import utils.InputOption
import utils.countryOptions.CountryOptionsNonUK
import views.html.asset.business.BusinessInternationalAddressView

class BusinessInternationalAddressControllerSpec extends SpecBase with IndexValidation {

  val formProvider             = new InternationalAddressFormProvider()
  val form: Form[NonUkAddress] = formProvider()

  val businessName = "Test"

  val validAnswer: NonUkAddress = NonUkAddress("line 1", "line 2", Some("line 3"), "country")

  lazy val businessInternationalAddressRoute: String =
    routes.BusinessInternationalAddressController.onPageLoad(index, NormalMode).url

  val baseAnswers: UserAnswers = emptyUserAnswers
    .set(BusinessNamePage(index), businessName)
    .success
    .value

  "AssetInternationalAddress Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, businessInternationalAddressRoute)

      val result = route(application, request).value

      val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptionsNonUK].options()

      val view = application.injector.instanceOf[BusinessInternationalAddressView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, index, countryOptions, NormalMode, businessName)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers
        .set(BusinessInternationalAddressPage(index), validAnswer)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, businessInternationalAddressRoute)

      val view = application.injector.instanceOf[BusinessInternationalAddressView]

      val result = route(application, request).value

      val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptionsNonUK].options()

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), index, countryOptions, NormalMode, businessName)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[Navigator].qualifiedWith(classOf[Business]).toInstance(fakeNavigator))
          .build()

      val request =
        FakeRequest(POST, businessInternationalAddressRoute)
          .withFormUrlEncodedBody(
            ("line1", validAnswer.line1),
            ("line2", validAnswer.line2),
            ("country", validAnswer.country)
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, businessInternationalAddressRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[BusinessInternationalAddressView]

      val result = route(application, request).value

      val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptionsNonUK].options()

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, countryOptions, NormalMode, businessName)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, businessInternationalAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, businessInternationalAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

  }

}
