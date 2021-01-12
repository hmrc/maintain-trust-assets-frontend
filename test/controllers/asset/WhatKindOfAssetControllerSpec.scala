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

package controllers.asset

import base.SpecBase
import controllers.IndexValidation
import controllers.routes._
import forms.WhatKindOfAssetFormProvider
import models.Status.Completed
import models.WhatKindOfAsset
import models.WhatKindOfAsset._
import org.scalacheck.Arbitrary.arbitrary
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import views.html.asset.WhatKindOfAssetView

class WhatKindOfAssetControllerSpec extends SpecBase with IndexValidation {

  private val index = 0

  private def whatKindOfAssetRoute(index: Int = index): String = routes.WhatKindOfAssetController.onPageLoad(index, fakeDraftId).url

  private val formProvider = new WhatKindOfAssetFormProvider()
  private val form = formProvider()

  private val options = WhatKindOfAsset.options()
  private val optionsWithoutMoney = WhatKindOfAsset.options().filterNot(_.value == Money.toString)

  "WhatKindOfAsset Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, whatKindOfAssetRoute())

      val result = route(application, request).value

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, fakeDraftId, index, options)(fakeRequest, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(WhatKindOfAssetPage(index), Shares).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, whatKindOfAssetRoute())

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(Shares), fakeDraftId, index, options)(fakeRequest, messages).toString

      application.stop()
    }

    "display Money if the same index is an in progress Money asset" in {

      val userAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage(index), Money).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, whatKindOfAssetRoute())

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(Money), fakeDraftId, index, options)(request, messages).toString

      application.stop()
    }

    "not display Money if an in progress or complete Money asset already exists for a different index" when {

      val baseAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage(0), Money).success.value

      "it's in progress" in {

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

        val request = FakeRequest(GET, whatKindOfAssetRoute(index = 1))

        val view = application.injector.instanceOf[WhatKindOfAssetView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, fakeDraftId, 1, optionsWithoutMoney)(request, messages).toString

        application.stop()
      }

      "it's complete" in {

        val userAnswers = baseAnswers
          .set(AssetStatus(0), Completed).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, whatKindOfAssetRoute(index = 1))

        val view = application.injector.instanceOf[WhatKindOfAssetView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, fakeDraftId, 1, optionsWithoutMoney)(request, messages).toString

        application.stop()
      }
    }


    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, whatKindOfAssetRoute())
          .withFormUrlEncodedBody(("value", WhatKindOfAsset.options().head.value))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, whatKindOfAssetRoute())
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, fakeDraftId, index, options)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, whatKindOfAssetRoute())

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, whatKindOfAssetRoute())
          .withFormUrlEncodedBody(("value", WhatKindOfAsset.values.head.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "for a GET" must {

      def getForIndex(index: Int): FakeRequest[AnyContentAsEmpty.type] = {
        val route = routes.WhatKindOfAssetController.onPageLoad(index, fakeDraftId).url

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
          routes.WhatKindOfAssetController.onPageLoad(index, fakeDraftId).url

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
