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

package controllers.asset.noneeabusiness

import base.SpecBase
import controllers.IndexValidation
import controllers.routes._
import forms.InternationalAddressFormProvider
import models.{InternationalAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.asset.noneeabusiness.{InternationalAddressPage, NamePage}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import utils.InputOption
import utils.countryOptions.CountryOptionsNonUK
import views.html.asset.noneeabusiness.InternationalAddressView

class InternationalAddressControllerSpec extends SpecBase with IndexValidation {

  private val formProvider = new InternationalAddressFormProvider()
  private val form: Form[InternationalAddress] = formProvider()
  private val index = 0
  private val name = "Test"
  private val validAnswer: InternationalAddress = InternationalAddress("value 1", "value 2", Some("value 3"), "FR")

  private lazy val onPageLoadRoute: String = routes.InternationalAddressController.onPageLoad(index, fakeDraftId).url

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(NamePage(index), name).success.value

  private val countryOptions: Seq[InputOption] = injector.instanceOf[CountryOptionsNonUK].options

  "InternationalAddressController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val view = application.injector.instanceOf[InternationalAddressView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, countryOptions, index, fakeDraftId, name)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers
        .set(InternationalAddressPage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val view = application.injector.instanceOf[InternationalAddressView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), countryOptions, index, fakeDraftId, name)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to name page when name is not answered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.NameController.onPageLoad(index, fakeDraftId).url

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, onPageLoadRoute)
          .withFormUrlEncodedBody(
            ("line1", validAnswer.line1),
            ("line2", validAnswer.line2),
            ("line3", validAnswer.line3.get),
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
        FakeRequest(POST, onPageLoadRoute)
          .withFormUrlEncodedBody(("line1", "invalid value"))

      val boundForm = form.bind(Map("line1" -> "invalid value"))

      val view = application.injector.instanceOf[InternationalAddressView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, countryOptions, index, fakeDraftId, name)(fakeRequest, messages).toString

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

      val request =
        FakeRequest(POST, onPageLoadRoute)
          .withFormUrlEncodedBody(
            ("line1", validAnswer.line1),
            ("line2", validAnswer.line2),
            ("line3", validAnswer.line3.get),
            ("country", validAnswer.country)
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "for a GET" must {

      def getForIndex(index: Int) : FakeRequest[AnyContentAsEmpty.type] = {
        val route = routes.InternationalAddressController.onPageLoad(index, fakeDraftId).url

        FakeRequest(GET, route)
      }

      validateIndex(
        arbitrary[InternationalAddress],
        InternationalAddressPage.apply,
        getForIndex
      )

    }

    "for a POST" must {
      def postForIndex(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] = {

        val route =
          routes.InternationalAddressController.onPageLoad(index, fakeDraftId).url

        FakeRequest(POST, route)
          .withFormUrlEncodedBody(
            ("line1", validAnswer.line1),
            ("line2", validAnswer.line2),
            ("line3", validAnswer.line3.get),
            ("country", validAnswer.country)
          )
      }

      validateIndex(
        arbitrary[InternationalAddress],
        InternationalAddressPage.apply,
        postForIndex
      )
    }

  }
}
