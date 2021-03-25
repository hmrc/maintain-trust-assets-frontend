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
import models.UserAnswers
import models.WhatKindOfAsset.NonEeaBusiness
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.asset.WhatKindOfAssetPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.asset.{NonTaxableInfoView, TaxableInfoView}

import scala.concurrent.Future

class AssetInterruptPageControllerSpec extends SpecBase {

  "AssetInterruptPage Controller" must {

    "return OK and the correct view for a GET" when {

      "4mld" in {

        val is5mldEnabled: Boolean = false

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(is5mldEnabled = is5mldEnabled))).build()

        val request = FakeRequest(GET, routes.AssetInterruptPageController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TaxableInfoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(is5mldEnabled)(request, messages).toString

        application.stop()
      }

      "5mld" when {

        "taxable" in {

          val is5mldEnabled: Boolean = true
          val isTaxable: Boolean = true

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(is5mldEnabled = is5mldEnabled, isTaxable = isTaxable))).build()

          val request = FakeRequest(GET, routes.AssetInterruptPageController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[TaxableInfoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(is5mldEnabled)(request, messages).toString

          application.stop()
        }

        "non-taxable" in {

          val is5mldEnabled: Boolean = true
          val isTaxable: Boolean = false

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(is5mldEnabled = is5mldEnabled, isTaxable = isTaxable))).build()

          val request = FakeRequest(GET, routes.AssetInterruptPageController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[NonTaxableInfoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view()(request, messages).toString

          application.stop()
        }
      }
    }

    "redirect to the correct page for a POST" when {

      "taxable" must {

        val isTaxable: Boolean = true

        "not set value in WhatKindOfAssetPage" in {

          reset(playbackRepository)
          when(playbackRepository.set(any())).thenReturn(Future.successful(true))
          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isTaxable = isTaxable))).build()

          val request = FakeRequest(POST, routes.AssetInterruptPageController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustBe fakeNavigator.desiredRoute.url

          verify(playbackRepository).set(uaCaptor.capture)
          uaCaptor.getValue.get(WhatKindOfAssetPage) mustNot be(defined)

          application.stop()
        }
      }

      "non-taxable" must {

        val isTaxable: Boolean = false

        "set value in WhatKindOfAssetPage" in {

          reset(playbackRepository)
          when(playbackRepository.set(any())).thenReturn(Future.successful(true))
          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isTaxable = isTaxable))).build()

          val request = FakeRequest(POST, routes.AssetInterruptPageController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustBe fakeNavigator.desiredRoute.url

          verify(playbackRepository).set(uaCaptor.capture)
          uaCaptor.getValue.get(WhatKindOfAssetPage).get mustBe NonEeaBusiness

          application.stop()
        }
      }
    }
  }
}
