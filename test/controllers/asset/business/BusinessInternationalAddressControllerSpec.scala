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

package controllers.asset.business

import base.SpecBase
import controllers.IndexValidation
import controllers.routes._
import forms.InternationalAddressFormProvider
import models.{InternationalAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.asset.business.{BusinessInternationalAddressPage, BusinessNamePage}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import utils.InputOption
import utils.countryOptions.CountryOptionsNonUK
import views.html.asset.buisness.BusinessInternationalAddressView

class BusinessInternationalAddressControllerSpec extends SpecBase with IndexValidation {

  val formProvider = new InternationalAddressFormProvider()
  val form: Form[InternationalAddress] = formProvider()

  val index = 0
  val businessName = "Test"

  val validAnswer: InternationalAddress = InternationalAddress("line 1", "line 2", Some("line 3"), "country")

  lazy val businessInternationalAddressRoute: String = routes.BusinessInternationalAddressController.onPageLoad(index).url

  val baseAnswers: UserAnswers = emptyUserAnswers
    .set(BusinessNamePage(index), businessName).success.value

  "AssetInternationalAddress Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, businessInternationalAddressRoute)

      val result = route(application, request).value

      val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptionsNonUK].options

      val view = application.injector.instanceOf[BusinessInternationalAddressView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, countryOptions, index, businessName)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers
        .set(BusinessInternationalAddressPage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, businessInternationalAddressRoute)

      val view = application.injector.instanceOf[BusinessInternationalAddressView]

      val result = route(application, request).value

      val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptionsNonUK].options

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), countryOptions, index, businessName)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to AssetNamePage when TrusteeOrgName is not answered" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, businessInternationalAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BusinessNameController.onPageLoad(index).url

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers)).build()

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

      val countryOptions: Seq[InputOption] = app.injector.instanceOf[CountryOptionsNonUK].options

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, countryOptions, index, businessName)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, businessInternationalAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, businessInternationalAddressRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "for a GET" must {

      def getForIndex(index: Int): FakeRequest[AnyContentAsEmpty.type] = {
        val route = routes.BusinessInternationalAddressController.onPageLoad(index).url

        FakeRequest(GET, route)
      }

      validateIndex(
        arbitrary[InternationalAddress],
        BusinessInternationalAddressPage.apply,
        getForIndex
      )

    }

    "for a POST" must {

      def postForIndex(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] = {

        val route =
          routes.BusinessInternationalAddressController.onPageLoad(index).url

        FakeRequest(POST, route)
          .withFormUrlEncodedBody(
            ("line1", validAnswer.line1),
            ("line2", validAnswer.line2),
            ("country", validAnswer.country)
          )
      }

      validateIndex(
        arbitrary[InternationalAddress],
        BusinessInternationalAddressPage.apply,
        postForIndex
      )
    }
  }
}