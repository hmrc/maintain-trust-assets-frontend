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
import pages.asset.shares.SharePortfolioQuantityInTrustPage
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import views.html.asset.shares.SharePortfolioQuantityInTrustView

class SharePortfolioQuantityInTrustControllerSpec extends SpecBase with ModelGenerators with IndexValidation {

  private val formProvider = new QuantityFormProvider(frontendAppConfig)
  private val form: Form[Long] = formProvider.withPrefix("shares.portfolioQuantityInTrust")
  private val index: Int = 0
  private val validAnswer: Long = 4000L

  private lazy val sharePortfolioQuantityInTrustRoute: String = routes.SharePortfolioQuantityInTrustController.onPageLoad(index).url

  "SharePortfolioQuantityInTrust Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, sharePortfolioQuantityInTrustRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[SharePortfolioQuantityInTrustView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, index)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(SharePortfolioQuantityInTrustPage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, sharePortfolioQuantityInTrustRoute)

      val view = application.injector.instanceOf[SharePortfolioQuantityInTrustView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), index)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, sharePortfolioQuantityInTrustRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, sharePortfolioQuantityInTrustRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[SharePortfolioQuantityInTrustView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, sharePortfolioQuantityInTrustRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, sharePortfolioQuantityInTrustRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

  }

  "for a GET" must {

    def getForIndex(index: Int) : FakeRequest[AnyContentAsEmpty.type] = {
      val route = routes.SharePortfolioQuantityInTrustController.onPageLoad(index).url

      FakeRequest(GET, route)
    }

    validateIndex(
      arbitrary[Long],
      SharePortfolioQuantityInTrustPage.apply,
      getForIndex
    )

  }

  "for a POST" must {
    def postForIndex(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] = {

      val route =
        routes.SharePortfolioQuantityInTrustController.onPageLoad(index).url

      FakeRequest(POST, route)
        .withFormUrlEncodedBody(("currency", validAnswer.toString))
    }

    validateIndex(
      arbitrary[Long],
      SharePortfolioQuantityInTrustPage.apply,
      postForIndex
    )
  }
}
