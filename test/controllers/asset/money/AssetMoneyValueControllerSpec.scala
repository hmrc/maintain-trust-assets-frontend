/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.asset.money

import base.SpecBase
import config.annotations.Money
import connectors.TrustsConnector
import controllers.routes._
import forms.ValueFormProvider
import models.NormalMode
import models.assets.AssetMonetaryAmount
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.asset.money.AssetMoneyValuePage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.HttpResponse
import views.html.asset.money.AssetMoneyValueView

import scala.concurrent.Future

class AssetMoneyValueControllerSpec extends SpecBase {

  val formProvider     = new ValueFormProvider(frontendAppConfig)
  val form: Form[Long] = formProvider.withConfig(prefix = "money.value")

  val mockTrustService: TrustService = mock[TrustService]
  val mockConnector: TrustsConnector = mock[TrustsConnector]

  val validAnswer: Long = 4000L

  lazy val assetMoneyValueRoute: String = routes.AssetMoneyValueController.onPageLoad(index, NormalMode).url

  "AssetMoneyValue Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind(classOf[TrustService]).toInstance(mockTrustService)
        )
        .build()

      when(mockTrustService.getMonetaryAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(AssetMonetaryAmount(validAnswer)))

      val request = FakeRequest(GET, assetMoneyValueRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AssetMoneyValueView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, index, NormalMode)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(AssetMoneyValuePage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind(classOf[TrustService]).toInstance(mockTrustService)
        )
        .build()

      when(mockTrustService.getMonetaryAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(AssetMonetaryAmount(validAnswer)))

      val request = FakeRequest(GET, assetMoneyValueRoute)

      val view = application.injector.instanceOf[AssetMoneyValueView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), index, NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            Seq(
              bind[Navigator].qualifiedWith(classOf[Money]).toInstance(fakeNavigator),
              bind(classOf[TrustService]).toInstance(mockTrustService),
              bind[TrustsConnector].to(mockConnector)
            )
          )
          .build()

      when(mockTrustService.getMonetaryAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(AssetMonetaryAmount(validAnswer)))

      when(mockConnector.addMoneyAsset(any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request =
        FakeRequest(POST, assetMoneyValueRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind(classOf[TrustService]).toInstance(mockTrustService)
        )
        .build()

      when(mockTrustService.getMonetaryAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(AssetMonetaryAmount(validAnswer)))

      val request =
        FakeRequest(POST, assetMoneyValueRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[AssetMoneyValueView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, assetMoneyValueRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, assetMoneyValueRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

}
