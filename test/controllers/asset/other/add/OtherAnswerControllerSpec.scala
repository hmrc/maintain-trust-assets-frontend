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

package controllers.asset.other.add

import base.SpecBase
import connectors.TrustsConnector
import controllers.routes._
import models.Status.Completed
import models.UserAnswers
import models.WhatKindOfAsset.Other
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.AssetStatus
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

  lazy val otherAnswerRoute: String = routes.OtherAnswerController.onPageLoad().url

  val answers: UserAnswers =
    emptyUserAnswers
      .set(WhatKindOfAssetPage, Other).success.value
      .set(OtherAssetDescriptionPage, "Other asset").success.value
      .set(OtherAssetValuePage, 4000L).success.value
      .set(AssetStatus, Completed).success.value

  "OtherAnswer Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, otherAnswerRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[OtherAssetAnswersView]
      val printHelper = application.injector.instanceOf[OtherPrintHelper]
      val answerSection = printHelper(answers, provisional = true, description)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answerSection)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
        .build()

      when(mockTrustConnector.addOtherAsset(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, controllers.asset.other.add.routes.OtherAnswerController.onSubmit().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }


    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, otherAnswerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
