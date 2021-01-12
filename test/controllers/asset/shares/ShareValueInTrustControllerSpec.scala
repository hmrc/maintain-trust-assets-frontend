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
import forms.ValueFormProvider
import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import pages.asset.shares.{ShareCompanyNamePage, ShareValueInTrustPage}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import views.html.asset.shares.ShareValueInTrustView

class ShareValueInTrustControllerSpec extends SpecBase with ModelGenerators with IndexValidation {

  val formProvider = new ValueFormProvider(frontendAppConfig)
  val form: Form[Long] = formProvider.withConfig(prefix = "shares.valueInTrust")
  val index: Int = 0
  val validAnswer: Long = 4000L
  val companyName = "Company"

  lazy val shareValueInTrustRoute: String = routes.ShareValueInTrustController.onPageLoad(index, fakeDraftId).url

  "ShareValueInTrust Controller" must {

    "return OK and the correct view for a GET" in {

      val ua = emptyUserAnswers.set(ShareCompanyNamePage(0), "Company").success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, shareValueInTrustRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ShareValueInTrustView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, fakeDraftId, index, companyName)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val ua = emptyUserAnswers.set(ShareCompanyNamePage(0), "Company").success.value
        .set(ShareValueInTrustPage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(ua)).build()

      val request = FakeRequest(GET, shareValueInTrustRoute)

      val view = application.injector.instanceOf[ShareValueInTrustView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), fakeDraftId, index, companyName)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val ua = emptyUserAnswers.set(ShareCompanyNamePage(0), "Company").success.value

      val application =
        applicationBuilder(userAnswers = Some(ua)).build()

      val request =
        FakeRequest(POST, shareValueInTrustRoute)
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
        FakeRequest(POST, shareValueInTrustRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[ShareValueInTrustView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, fakeDraftId, index, companyName)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, shareValueInTrustRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, shareValueInTrustRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to ShareCompanyNamePage when company name is not answered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, shareValueInTrustRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.ShareCompanyNameController.onPageLoad(index, fakeDraftId).url

      application.stop()
    }
  }

  "for a GET" must {

    def getForIndex(index: Int) : FakeRequest[AnyContentAsEmpty.type] = {
      val route = routes.ShareValueInTrustController.onPageLoad(index, fakeDraftId).url

      FakeRequest(GET, route)
    }

    validateIndex(
      arbitrary[Long],
      ShareValueInTrustPage.apply,
      getForIndex
    )

  }

  "for a POST" must {
    def postForIndex(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] = {

      val route =
        routes.ShareValueInTrustController.onPageLoad(index, fakeDraftId).url

      FakeRequest(POST, route)
        .withFormUrlEncodedBody(("value", validAnswer.toString))
    }

    validateIndex(
      arbitrary[Long],
      ShareValueInTrustPage.apply,
      postForIndex
    )
  }
}
