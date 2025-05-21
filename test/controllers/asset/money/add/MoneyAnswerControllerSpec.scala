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

package controllers.asset.money.add



import base.SpecBase
import connectors.TrustsConnector
import controllers.routes._
import models.Status.Completed
import models.UserAnswers
import models.WhatKindOfAsset.Money
import models.assets.AssetMonetaryAmount
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.money.AssetMoneyValuePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.HttpResponse
import utils.print.MoneyPrintHelper
import views.html.asset.money.MoneyAnswersView

import scala.concurrent.Future

class MoneyAnswerControllerSpec extends SpecBase {

  val description: String = "Money asset"

  lazy val moneyAnswerRoute: String = routes.MoneyAnswerController.onPageLoad().url

  val answers: UserAnswers =
    emptyUserAnswers
      .set(WhatKindOfAssetPage, Money).success.value
      .set(AssetMoneyValuePage, 4000L).success.value
      .set(AssetStatus, Completed).success.value

  "MoneyAnswer Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, moneyAnswerRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[MoneyAnswersView]
      val printHelper = application.injector.instanceOf[MoneyPrintHelper]
      val answerSection = printHelper(answers, provisional = true, description)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answerSection)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      // Mocking the TrustService to return a monetary asset
      val mockTrustService = mock[TrustService]
      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[TrustService].toInstance(mockTrustService),
          bind[TrustsConnector].toInstance(mockTrustConnector)
        )
        .build()

      // Mocking getMonetaryAsset to return a valid asset
      val validMonetaryAsset = AssetMonetaryAmount(4000L)
      when(mockTrustService.getMonetaryAsset(any())(any(), any())).thenReturn(Future.successful(Some(validMonetaryAsset)))

      // Mocking the amendMoneyAsset method to return a successful response (this is crucial!)
      when(mockTrustConnector.amendMoneyAsset(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, controllers.asset.money.add.routes.MoneyAnswerController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "4000")

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, moneyAnswerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}