/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.IndexValidation
import controllers.routes._
import forms.ValueFormProvider
import models.{NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.asset.property_or_land.PropertyLandValueTrustPage
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.asset.property_or_land.PropertyLandValueTrustView

class PropertyLandValueTrustControllerSpec extends SpecBase with IndexValidation {

  val formProvider = new ValueFormProvider()
  val form = formProvider.withConfig("propertyOrLand.valueInTrust")

  val index: Int = 0

  lazy val propertyLandValueTrustRoute = routes.PropertyLandValueTrustController.onPageLoad(NormalMode, index, fakeDraftId).url

  val userAnswers = UserAnswers(
    draftId = fakeDraftId,
    data = Json.obj(
      PropertyLandValueTrustPage.toString -> Json.obj(
        "value" -> "value 1"
      )
    ),
    internalAuthId = userInternalId
  )

  "PropertyLandValueTrust Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, propertyLandValueTrustRoute)

      val view = application.injector.instanceOf[PropertyLandValueTrustView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, index, fakeDraftId)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(PropertyLandValueTrustPage(index), "1").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, propertyLandValueTrustRoute)

      val view = application.injector.instanceOf[PropertyLandValueTrustView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill("1"), NormalMode, index, fakeDraftId)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, propertyLandValueTrustRoute)
          .withFormUrlEncodedBody(("value", "100"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, propertyLandValueTrustRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[PropertyLandValueTrustView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, index, fakeDraftId)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, propertyLandValueTrustRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, propertyLandValueTrustRoute)
          .withFormUrlEncodedBody(("field1", "value 1"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "for a GET" must {

      def getForIndex(index: Int) : FakeRequest[AnyContentAsEmpty.type] = {
        val route = routes.PropertyLandValueTrustController.onPageLoad(NormalMode, index, fakeDraftId).url

        FakeRequest(GET, route)
      }

      validateIndex(
        arbitrary[String],
        PropertyLandValueTrustPage.apply,
        getForIndex
      )

    }

    "for a POST" must {

      def postForIndex(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] = {

        val route =
          routes.PropertyLandValueTrustController.onPageLoad(NormalMode, index, fakeDraftId).url

        FakeRequest(POST, route)
          .withFormUrlEncodedBody(("value", "1234"))
      }

      validateIndex(
        arbitrary[String],
        PropertyLandValueTrustPage.apply,
        postForIndex
      )
    }
  }
}
