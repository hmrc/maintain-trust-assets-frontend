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
import config.annotations.Assets
import navigation.Navigator
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import views.html.asset.nonTaxableToTaxable.{AssetInterruptView => MigrationInteruptPage}
import views.html.asset.noneeabusiness.AssetInterruptView

import scala.concurrent.Future

class AssetInterruptPageControllerSpec extends SpecBase {

  "AssetInterruptPage Controller" must {

    "return OK and the correct view for a GET" when {

      "nonEEA" in {

        val is5mldEnabled: Boolean = true
        val isTaxable: Boolean = true
        val migrating: Boolean = false

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(is5mldEnabled = is5mldEnabled, isTaxable = isTaxable, isMigratingToTaxable = migrating ))).build()

        val request = FakeRequest(GET, routes.AssetInterruptPageController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AssetInterruptView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages).toString

        application.stop()
      }

      "migrating" in {

        val is5mldEnabled: Boolean = true
        val isTaxable: Boolean = true
        val migrating: Boolean = true

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(is5mldEnabled = is5mldEnabled, isTaxable = isTaxable, isMigratingToTaxable = migrating))).build()

        val request = FakeRequest(GET, routes.AssetInterruptPageController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MigrationInteruptPage]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages).toString

        application.stop()
      }

    }

    "redirect to the correct page for a POST" when {
      "taxable" must {

        val isTaxable: Boolean = true

        "not set value in WhatKindOfAssetPage" in {
          
          val mockTrustService: TrustService = mock[TrustService]

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isTaxable = isTaxable)))
            .overrides(bind[Navigator].qualifiedWith(classOf[Assets]).toInstance(fakeNavigator))
            .overrides(bind[TrustService].toInstance(mockTrustService))
            .build()

          when(mockTrustService.getAssets(any())(any(), any())).thenReturn(Future.successful(models.assets.Assets()))

          val request = FakeRequest(POST, routes.AssetInterruptPageController.onSubmit().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustBe fakeNavigator.desiredRoute.url

          application.stop()
        }
      }
    }
  }
}
