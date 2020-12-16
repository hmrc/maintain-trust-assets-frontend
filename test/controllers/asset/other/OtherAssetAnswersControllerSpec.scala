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

package controllers.asset.other

import base.SpecBase
import models.UserAnswers
import models.WhatKindOfAsset.Other
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.asset._
import pages.asset.other._
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.print.OtherPrintHelper
import views.html.asset.other.OtherAssetAnswersView

import scala.concurrent.Future

class OtherAssetAnswersControllerSpec extends SpecBase {

  "OtherAssetAnswersController" must {

    val index: Int = 0
    val description: String = "Description"

    lazy val answersRoute = routes.OtherAssetAnswersController.onPageLoad(index, fakeDraftId).url

    val baseAnswers: UserAnswers = emptyUserAnswers
      .set(WhatKindOfAssetPage(index), Other).success.value
      .set(OtherAssetDescriptionPage(index), description).success.value
      .set(OtherAssetValuePage(index), 4000L).success.value

    "return OK and the correct view for a GET" in {

      val expectedSections = Nil
      val mockPrintHelper: OtherPrintHelper = mock[OtherPrintHelper]
      when(mockPrintHelper.checkDetailsSection(any(), any(), any(), any())(any())).thenReturn(Nil)

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(bind[OtherPrintHelper].toInstance(mockPrintHelper))
        .build()

      val request = FakeRequest(GET, answersRoute)

      val result: Future[Result] = route(application, request).value

      val view: OtherAssetAnswersView = application.injector.instanceOf[OtherAssetAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to description page if no description found" in {

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage(index), Other).success.value

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, answersRoute)

      val result: Future[Result] = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad(index, fakeDraftId).url

      application.stop()
    }

    "redirect to value page if description found but no value found" in {

      val userAnswers: UserAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage(index), Other).success.value
        .set(OtherAssetDescriptionPage(index), description).success.value

      val application: Application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, answersRoute)

      val result: Future[Result] = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.asset.other.routes.OtherAssetValueController.onPageLoad(index, fakeDraftId).url

      application.stop()
    }

    "redirect to add assets on submission" in {

      val application: Application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(POST, answersRoute)

      val result: Future[Result] = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.asset.routes.AddAssetsController.onPageLoad(fakeDraftId).url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, answersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual
        controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
