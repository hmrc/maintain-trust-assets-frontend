/*
 * Copyright 2024 HM Revenue & Customs
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
import models.WhatKindOfAsset
import models.WhatKindOfAsset._
import models.assets.Assets
import navigation.AssetsNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.asset.WhatKindOfAssetPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{route, _}
import services.TrustService
import views.html.asset.WhatKindOfAssetView

import scala.concurrent.Future

class WhatKindOfAssetControllerSpec extends SpecBase with IndexValidation {

  private def whatKindOfAssetRoute(index: Int): String = routes.WhatKindOfAssetController.onPageLoad(index).url

  private val formProvider = new WhatKindOfAssetFormProvider()
  private val form = formProvider()

  private val mockTrustService: TrustService = mock[TrustService]

  private val optionsFor5mld = WhatKindOfAsset.options()

  "WhatKindOfAsset Controller" must {

    val options = optionsFor5mld
    val baseAnswers = emptyUserAnswers.copy(is5mldEnabled = true)

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(
          bind[TrustService].to(mockTrustService)
        )
        .build()

      when(mockTrustService.getAssets(any())(any(), any())).thenReturn(Future.successful(Assets()))

      val request = FakeRequest(GET, whatKindOfAssetRoute(index))

      val result = route(application, request).value

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, 0, options)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers
        .set(WhatKindOfAssetPage(index), Shares).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustService].to(mockTrustService)
        )
        .build()

      when(mockTrustService.getAssets(any())(any(), any())).thenReturn(Future.successful(Assets()))

      val request = FakeRequest(GET, whatKindOfAssetRoute(index))

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(Shares), 0, options)(request, messages).toString

      application.stop()
    }

    "display Money if the same index is an in progress Money asset" in {

      val userAnswers = baseAnswers
        .set(WhatKindOfAssetPage(index), Money).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustService].to(mockTrustService)
        )
        .build()

      when(mockTrustService.getAssets(any())(any(), any())).thenReturn(Future.successful(Assets()))

      val request = FakeRequest(GET, whatKindOfAssetRoute(index))

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(Money), 0, options)(request, messages).toString

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers))
        .overrides(
          bind[TrustService].to(mockTrustService)
        )
        .build()

      when(mockTrustService.getAssets(any())(any(), any())).thenReturn(Future.successful(Assets()))

      val request =
        FakeRequest(POST, whatKindOfAssetRoute(index))
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[WhatKindOfAssetView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, 0, options)(request, messages).toString

      application.stop()
    }
  }

  "redirect to the next page when valid data is submitted" in {

    val mockNavigator: AssetsNavigator = mock[AssetsNavigator]
    when(mockNavigator.addAssetNowRoute(any(), any())).thenReturn(fakeNavigator.desiredRoute) // TODO: Review change??

    val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
      .overrides(
        bind[AssetsNavigator].toInstance(mockNavigator),
        bind[TrustService].to(mockTrustService)
      ).build()

    when(mockTrustService.getAssets(any())(any(), any())).thenReturn(Future.successful(Assets()))

    val request =
      FakeRequest(POST, whatKindOfAssetRoute(index))
        .withFormUrlEncodedBody(("value", WhatKindOfAsset.options().head.value))

    val result = route(application, request).value

    status(result) mustEqual SEE_OTHER

    redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

    application.stop()
  }

  "redirect to Session Expired for a GET if no existing data is found" in {

    val application = applicationBuilder(userAnswers = None).build()

    val request = FakeRequest(GET, whatKindOfAssetRoute(index))

    val result = route(application, request).value

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

    application.stop()
  }

  "redirect to Session Expired for a POST if no existing data is found" in {

    val application = applicationBuilder(userAnswers = None).build()

    val request =
      FakeRequest(POST, whatKindOfAssetRoute(index))
        .withFormUrlEncodedBody(("value", WhatKindOfAsset.values.head.toString))

    val result = route(application, request).value

    status(result) mustEqual SEE_OTHER

    redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

    application.stop()
  }
}
