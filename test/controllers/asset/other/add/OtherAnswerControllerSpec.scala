/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.asset.other.add

import base.SpecBase
import connectors.TrustsConnector
import mapping.OtherAssetMapper
import models.UserAnswers
import models.WhatKindOfAsset.Other
import models.assets._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import pages.asset.WhatKindOfAssetPage
import pages.asset.other.{OtherAssetDescriptionPage, OtherAssetValuePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.OtherPrintHelper
import views.html.asset.other.add.OtherAssetAnswersView

import scala.concurrent.Future

class OtherAnswerControllerSpec extends SpecBase {

  val description: String = "Other asset"

  lazy val otherAnswerRoute: String = controllers.asset.other.add.routes.OtherAnswerController.onPageLoad(index).url

  val answers: UserAnswers =
    emptyUserAnswers
      .set(WhatKindOfAssetPage(index), Other).success.value
      .set(OtherAssetDescriptionPage(index), "Other asset").success.value
      .set(OtherAssetValuePage(index), 4000L).success.value

  private val mapped: OtherAssetType = OtherAssetType("Other asset", 4000L)
  private val different: OtherAssetType = OtherAssetType("Different asset", 999L)

  "OtherAnswer Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, otherAnswerRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[OtherAssetAnswersView]
      val printHelper = application.injector.instanceOf[OtherPrintHelper]
      val answerSection = printHelper(answers, index, provisional = true, description)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(index, answerSection)(request, messages).toString

      application.stop()
    }

    "return INTERNAL_SERVER_ERROR when mapper returns None" in {

      val mockTrustConnector = mock[TrustsConnector]
      val mockMapper = mock[OtherAssetMapper]

      when(mockMapper.apply(any())).thenReturn(None)

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[TrustsConnector].toInstance(mockTrustConnector),
          bind[OtherAssetMapper].toInstance(mockMapper)
        )
        .build()

      val request = FakeRequest(POST, controllers.asset.other.add.routes.OtherAnswerController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

    "amend branch: redirects on OK when amending the last existing other asset" in {

      val mockTrustConnector = mock[TrustsConnector]
      val mockMapper = mock[OtherAssetMapper]

      when(mockMapper.apply(any())).thenReturn(Some(mapped))
      when(mockTrustConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(models.assets.Assets(Nil, Nil, Nil, Nil, Nil, List(OtherAssetType("old", 1L)), Nil)))
      when(mockTrustConnector.amendOtherAsset(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[TrustsConnector].toInstance(mockTrustConnector),
          bind[OtherAssetMapper].toInstance(mockMapper)
        )
        .build()

      val request = FakeRequest(POST, controllers.asset.other.add.routes.OtherAnswerController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "amend branch: also redirects on NO_CONTENT" in {

      val mockTrustConnector = mock[TrustsConnector]
      val mockMapper = mock[OtherAssetMapper]

      when(mockMapper.apply(any())).thenReturn(Some(mapped))
      when(mockTrustConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(models.assets.Assets(Nil, Nil, Nil, Nil, Nil, List(OtherAssetType("old", 1L)), Nil)))
      when(mockTrustConnector.amendOtherAsset(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[TrustsConnector].toInstance(mockTrustConnector),
          bind[OtherAssetMapper].toInstance(mockMapper)
        )
        .build()

      val request = FakeRequest(POST, controllers.asset.other.add.routes.OtherAnswerController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "add branch: adds when asset does not already exist and redirects" in {

      val mockTrustConnector = mock[TrustsConnector]
      val mockMapper = mock[OtherAssetMapper]

      when(mockMapper.apply(any())).thenReturn(Some(mapped))
      when(mockTrustConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(models.assets.Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil)))
      when(mockTrustConnector.addOtherAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[TrustsConnector].toInstance(mockTrustConnector),
          bind[OtherAssetMapper].toInstance(mockMapper)
        )
        .build()

      val request = FakeRequest(POST, controllers.asset.other.add.routes.OtherAnswerController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "exists branch: does not add when exact same asset already exists and redirects" in {

      val mockTrustConnector = mock[TrustsConnector]
      val mockMapper = mock[OtherAssetMapper]

      when(mockMapper.apply(any())).thenReturn(Some(mapped))
      when(mockTrustConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(models.assets.Assets(
          Nil, Nil, Nil, Nil, Nil,
          List(OtherAssetType("Other asset", 4000L), different),
          Nil
        )))

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[TrustsConnector].toInstance(mockTrustConnector),
          bind[OtherAssetMapper].toInstance(mockMapper)
        )
        .build()

      val request = FakeRequest(POST, controllers.asset.other.add.routes.OtherAnswerController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      verify(mockTrustConnector, never()).addOtherAsset(any(), any())(any(), any())

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, otherAnswerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, controllers.asset.other.add.routes.OtherAnswerController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
