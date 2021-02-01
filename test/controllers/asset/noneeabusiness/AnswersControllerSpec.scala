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
import controllers.routes._
import models.{UKAddress, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.asset.noneeabusiness._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.print.NonEeaBusinessPrintHelper
import views.html.asset.noneeabusiness.AnswersView

import java.time.LocalDate

class AnswersControllerSpec extends SpecBase {

  private val index = 0

  private val answers: UserAnswers = emptyUserAnswers
    .set(NamePage(index), "Name").success.value
    .set(AddressUkYesNoPage(index), true).success.value
    .set(UkAddressPage(index), UKAddress("Line 1", "Line 2", None, None, "AB1 1AB")).success.value
    .set(GoverningCountryPage(index), "FR").success.value
    .set(StartDatePage(index), LocalDate.parse("1996-02-03")).success.value

  private lazy val onPageLoadRoute: String = routes.AnswersController.onSubmit(index, fakeDraftId).url

  "AnswersController" must {

    "return OK and the correct view for a GET" in {

      val expectedSections = Nil
      val mockPrintHelper: NonEeaBusinessPrintHelper = mock[NonEeaBusinessPrintHelper]
      when(mockPrintHelper.checkDetailsSection(any(), any(), any(), any())(any())).thenReturn(Nil)

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[NonEeaBusinessPrintHelper].toInstance(mockPrintHelper))
        .build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(POST, routes.AnswersController.onSubmit(index, fakeDraftId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.routes.AddAssetsController.onPageLoad(fakeDraftId).url

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

      val request = FakeRequest(POST, routes.AnswersController.onSubmit(index, fakeDraftId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

  }
}
