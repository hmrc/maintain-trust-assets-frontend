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

package controllers.asset.partnership

import java.time.{LocalDate, ZoneOffset}

import base.SpecBase
import controllers.IndexValidation
import forms.partnership.PartnershipStartDateFormProvider
import models.NormalMode
import org.scalacheck.Gen
import pages.asset.partnership.PartnershipStartDatePage
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import views.html.asset.partnership.PartnershipStartDateView

class PartnershipStartDateControllerSpec extends SpecBase with IndexValidation {

  private val formProvider = new PartnershipStartDateFormProvider(frontendAppConfig)
  private val form = formProvider.withConfig()
  private val index = 0

  private val validAnswer = LocalDate.now(ZoneOffset.UTC)

  private lazy val partnershipStartDateRoute = routes.PartnershipStartDateController.onPageLoad(NormalMode, index, fakeDraftId).url

  "PartnershipStartDate Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, partnershipStartDateRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[PartnershipStartDateView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, index, fakeDraftId)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(PartnershipStartDatePage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, partnershipStartDateRoute)

      val view = application.injector.instanceOf[PartnershipStartDateView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), NormalMode, index, fakeDraftId)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, partnershipStartDateRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, partnershipStartDateRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[PartnershipStartDateView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, index, fakeDraftId)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, partnershipStartDateRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, partnershipStartDateRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }

  "for a GET" must {

    def getForIndex(index: Int): FakeRequest[AnyContentAsEmpty.type] = {
      val route = routes.PartnershipStartDateController.onPageLoad(NormalMode, index, fakeDraftId).url

      FakeRequest(GET, route)
    }

    validateIndex(
      Gen.const(LocalDate.of(2010,10,10)),
      PartnershipStartDatePage.apply,
      getForIndex
    )

  }

  "for a POST" must {
    def postForIndex(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] = {

      val route =
        routes.PartnershipStartDateController.onPageLoad(NormalMode, index, fakeDraftId).url

      FakeRequest(POST, route)
        .withFormUrlEncodedBody(
          "value.day"   -> validAnswer.getDayOfMonth.toString,
          "value.month" -> validAnswer.getMonthValue.toString,
          "value.year"  -> validAnswer.getYear.toString
        )
    }

    validateIndex(
      Gen.const(LocalDate.of(2010,10,10)),
      PartnershipStartDatePage.apply,
      postForIndex
    )
  }

}
