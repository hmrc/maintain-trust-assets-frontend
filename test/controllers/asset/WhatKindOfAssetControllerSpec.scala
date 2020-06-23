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

package controllers.asset

import base.SpecBase
import controllers.IndexValidation
import forms.WhatKindOfAssetFormProvider
import models.WhatKindOfAsset.Money
import models.{NormalMode, WhatKindOfAsset}
import org.scalacheck.Arbitrary.arbitrary
import pages.asset.WhatKindOfAssetPage
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import views.html.asset.WhatKindOfAssetView

class WhatKindOfAssetControllerSpec extends SpecBase with IndexValidation  {

  val index = 0

  lazy val whatKindOfAssetRoute = controllers.routes.WhatKindOfAssetController.onPageLoad(NormalMode, index, fakeDraftId).url

  val formProvider = new WhatKindOfAssetFormProvider()
  val form = formProvider()

  val options = WhatKindOfAsset.options
  val optionsWithoutMoney = WhatKindOfAsset.options.filterNot(_.value == Money.toString)

  "WhatKindOfAsset Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, whatKindOfAssetRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode, fakeDraftId, index, options)(fakeRequest, messages).toString

      application.stop()
    }

    "when money has been answered for the same index" must {

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = emptyUserAnswers.set(WhatKindOfAssetPage(index), Money).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, whatKindOfAssetRoute)

        val view = application.injector.instanceOf[WhatKindOfAssetView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(WhatKindOfAsset.values.head), NormalMode, fakeDraftId, index, options)(fakeRequest, messages).toString

        application.stop()
      }

    }

    "when money has been answered and viewing a different index" must {

      "populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = emptyUserAnswers.set(WhatKindOfAssetPage(index), Money).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, controllers.routes.WhatKindOfAssetController.onPageLoad(NormalMode, 1, fakeDraftId).url)

        val view = application.injector.instanceOf[WhatKindOfAssetView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(WhatKindOfAsset.values.head), NormalMode, fakeDraftId, 1, optionsWithoutMoney)(fakeRequest, messages).toString

        application.stop()
      }

    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, whatKindOfAssetRoute)
          .withFormUrlEncodedBody(("value", WhatKindOfAsset.options.head.value))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "redirect to the next page when money is submitted again for the same index" in {

      val answers = emptyUserAnswers.set(WhatKindOfAssetPage(index), Money).success.value

      val application =
        applicationBuilder(userAnswers = Some(answers)).build()

      val request =
        FakeRequest(POST, whatKindOfAssetRoute)
          .withFormUrlEncodedBody(("value", WhatKindOfAsset.Money.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, whatKindOfAssetRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, fakeDraftId, index, options)(fakeRequest, messages).toString

      application.stop()
    }

    "return a BadRequest when money is submitted and already exists for a different index" in {

      val answers = emptyUserAnswers.set(WhatKindOfAssetPage(index), Money).success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(controllers.routes.WhatKindOfAssetController.onSubmit(NormalMode, 1, fakeDraftId))
        .withFormUrlEncodedBody(("value", "Money"))

      val boundForm = form.bind(Map("value" -> "Money"))

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode, fakeDraftId, 1, optionsWithoutMoney)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, whatKindOfAssetRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, whatKindOfAssetRoute)
          .withFormUrlEncodedBody(("value", WhatKindOfAsset.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "for a GET" must {

      def getForIndex(index: Int): FakeRequest[AnyContentAsEmpty.type] = {
        val route = controllers.routes.WhatKindOfAssetController.onPageLoad(NormalMode, index, fakeDraftId).url

        FakeRequest(GET, route)
      }

      validateIndex(
        arbitrary[WhatKindOfAsset],
        WhatKindOfAssetPage.apply,
        getForIndex
      )

    }

    "for a POST" must {
      def postForIndex(index: Int): FakeRequest[AnyContentAsFormUrlEncoded] = {

        val route =
          controllers.routes.WhatKindOfAssetController.onPageLoad(NormalMode, index, fakeDraftId).url

        FakeRequest(POST, route)
          .withFormUrlEncodedBody(("value", WhatKindOfAsset.values.head.toString))
      }

      validateIndex(
        arbitrary[WhatKindOfAsset],
        WhatKindOfAssetPage.apply,
        postForIndex
      )
    }
  }
}
