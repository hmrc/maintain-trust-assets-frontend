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

package controllers.asset.business

import base.SpecBase
import controllers.routes._
import models.{UKAddress, UserAnswers}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.asset.business._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.print.BusinessPrintHelper
import views.html.asset.buisness.BusinessAnswersView

class BusinessAnswersControllerSpec extends SpecBase {

  val index = 0

  val answers: UserAnswers = emptyUserAnswers
    .set(BusinessNamePage(index), "test").success.value
    .set(BusinessDescriptionPage(index), "test test test").success.value
    .set(BusinessAddressUkYesNoPage(index), true).success.value
    .set(BusinessUkAddressPage(index), UKAddress("test", "test", None, None, "NE11NE")).success.value
    .set(BusinessValuePage(index), 12L).success.value

  "AssetAnswerPage Controller" must {

    "return OK and the correct view for a GET" in {

      val expectedSections = Nil
      val mockPrintHelper: BusinessPrintHelper = mock[BusinessPrintHelper]
      when(mockPrintHelper.checkDetailsSection(any(), any(), any(), any())(any())).thenReturn(Nil)

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[BusinessPrintHelper].toInstance(mockPrintHelper))
        .build()

      val request = FakeRequest(GET, routes.BusinessAnswersController.onPageLoad(index, fakeDraftId).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BusinessAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit(index, fakeDraftId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.routes.AddAssetsController.onPageLoad(fakeDraftId).url

      application.stop()
    }

    "redirect to AssetNamePage when valid data is submitted with no AssetName answer" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit(index, fakeDraftId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BusinessNameController.onPageLoad(index, fakeDraftId).url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.BusinessAnswersController.onPageLoad(index, fakeDraftId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit(index, fakeDraftId).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

  }
}
