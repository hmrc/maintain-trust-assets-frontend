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

package controllers.asset.shares

import base.SpecBase
import controllers.IndexValidation
import forms.QuantityFormProvider
import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import pages.asset.shares.{ShareCompanyNamePage, ShareQuantityInTrustPage}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import views.html.asset.shares.ShareQuantityInTrustView

class ShareQuantityInTrustControllerSpec extends SpecBase with ModelGenerators with IndexValidation {

  private val formProvider = new QuantityFormProvider(frontendAppConfig)
  private val form: Form[Long] = formProvider.withPrefix("shares.quantityInTrust")
  private val index: Int = 0
  private val companyName = "Company"
  private val validAnswer: Long = 4000L

  private lazy val shareQuantityInTrustRoute: String = routes.ShareQuantityInTrustController.onPageLoad(index).url

  "ShareQuantityInTrustController" must {

    "return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers.set(ShareCompanyNamePage(0), "Company").success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, shareQuantityInTrustRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ShareQuantityInTrustView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, index, companyName)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val ua = emptyUserAnswers.set(ShareCompanyNamePage(0), "Company").success.value
        .set(ShareQuantityInTrustPage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, shareQuantityInTrustRoute)

      val view = application.injector.instanceOf[ShareQuantityInTrustView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), index, companyName)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val ua = emptyUserAnswers.set(ShareCompanyNamePage(0), "Company").success.value

      val application =
        applicationBuilder(userAnswers = Some(ua)).build()

      val request =
        FakeRequest(POST, shareQuantityInTrustRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val ua = emptyUserAnswers.set(ShareCompanyNamePage(0), "Company").success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request =
        FakeRequest(POST, shareQuantityInTrustRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ShareQuantityInTrustView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, companyName)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, shareQuantityInTrustRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, shareQuantityInTrustRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to AssetsShareCompanyNamePage when company name is not answered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, shareQuantityInTrustRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ShareCompanyNameController.onPageLoad(index).url

      application.stop()
    }
  }

  "for a GET" must {

    def getForIndex(index: Int) : FakeRequest[AnyContentAsEmpty.type] = {
      val route = routes.ShareQuantityInTrustController.onPageLoad(index).url

      FakeRequest(GET, route)
    }

    validateIndex(
      arbitrary[Long],
      ShareQuantityInTrustPage.apply,
      getForIndex
    )

  }

  "for a POST" must {
    def postForIndex(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] = {

      val route =
        routes.ShareQuantityInTrustController.onPageLoad(index).url

      FakeRequest(POST, route)
        .withFormUrlEncodedBody(("currency", validAnswer.toString))
    }

    validateIndex(
      arbitrary[Long],
      ShareQuantityInTrustPage.apply,
      postForIndex
    )
  }
}
