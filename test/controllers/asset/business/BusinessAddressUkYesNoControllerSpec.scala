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
import forms.YesNoFormProvider
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import pages.asset.business.{BusinessAddressUkYesNoPage, BusinessNamePage}
import play.api.data.Form
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import views.html.asset.buisness.BusinessAddressUkYesNoView

class BusinessAddressUkYesNoControllerSpec extends SpecBase with IndexValidation {

  val formProvider = new YesNoFormProvider()
  val form: Form[Boolean] = formProvider.withPrefix("business.addressUkYesNo")

  val index = 0
  val businessName = "Test"

  lazy val businessAssetAddressUkYesNoRoute: String = routes.BusinessAddressUkYesNoController.onPageLoad(index, fakeDraftId).url

  val baseAnswers: UserAnswers = emptyUserAnswers
    .set(BusinessNamePage(index), businessName).success.value

  "BusinessAddressUkYesNoController Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, businessAssetAddressUkYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BusinessAddressUkYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, fakeDraftId, index, businessName)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers
        .set(BusinessAddressUkYesNoPage(index), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, businessAssetAddressUkYesNoRoute)

      val view = application.injector.instanceOf[BusinessAddressUkYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true), fakeDraftId, index, businessName)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to AssetNamePage when AssetName is not answered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, businessAssetAddressUkYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BusinessNameController.onPageLoad(index, fakeDraftId).url

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, businessAssetAddressUkYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, businessAssetAddressUkYesNoRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[BusinessAddressUkYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, fakeDraftId, index, businessName)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, businessAssetAddressUkYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, businessAssetAddressUkYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "for a GET" must {

      def getForIndex(index: Int): FakeRequest[AnyContentAsEmpty.type] = {
        val route = routes.BusinessAddressUkYesNoController.onPageLoad(index, fakeDraftId).url

        FakeRequest(GET, route)
      }

      validateIndex(
        arbitrary[Boolean],
        BusinessAddressUkYesNoPage.apply,
        getForIndex
      )

    }

    "for a POST" must {

      def postForIndex(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] = {

        val route = routes.BusinessAddressUkYesNoController.onPageLoad(index, fakeDraftId).url

        FakeRequest(POST, route)
          .withFormUrlEncodedBody(("value", "true"))
      }

      validateIndex(
        arbitrary[Boolean],
        BusinessAddressUkYesNoPage.apply,
        postForIndex
      )
    }
  }
}