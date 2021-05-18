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

package controllers.asset.money

import base.SpecBase
import config.annotations.Money
import controllers.routes._
import forms.ValueFormProvider
import models.NormalMode
import models.assets.{AssetMonetaryAmount, Assets}
import navigation.Navigator
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.asset.money.AssetMoneyValuePage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import views.html.asset.money.AssetMoneyValueView

import scala.concurrent.Future

class AssetMoneyValueControllerSpec extends SpecBase {

  val formProvider = new ValueFormProvider(frontendAppConfig)
  val form: Form[Long] = formProvider.withConfig(prefix = "money.value")

  val mockTrustService = mock[TrustService]

  val validAnswer: Long = 4000L

  lazy val assetMoneyValueRoute: String = routes.AssetMoneyValueController.onPageLoad(NormalMode).url

  "AssetMoneyValue Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind(classOf[TrustService]).toInstance(mockTrustService)
      ).build()

      when(mockTrustService.getMonetaryAsset(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val request = FakeRequest(GET, assetMoneyValueRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AssetMoneyValueView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, NormalMode)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(AssetMoneyValuePage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).overrides(
        bind(classOf[TrustService]).toInstance(mockTrustService)
      ).build()

      when(mockTrustService.getMonetaryAsset(any())(any(), any()))
        .thenReturn(Future.successful(Some(AssetMonetaryAmount(validAnswer))))

      val request = FakeRequest(GET, assetMoneyValueRoute)

      val view = application.injector.instanceOf[AssetMoneyValueView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(validAnswer), NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(Seq(
            bind[Navigator].qualifiedWith(classOf[Money]).toInstance(fakeNavigator),
            bind(classOf[TrustService]).toInstance(mockTrustService)
          )).build()

      when(mockTrustService.getMonetaryAsset(any())(any(), any()))
        .thenReturn(Future.successful(Some(AssetMonetaryAmount(validAnswer))))

      val request =
        FakeRequest(POST, assetMoneyValueRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind(classOf[TrustService]).toInstance(mockTrustService)
      ).build()

      when(mockTrustService.getMonetaryAsset(any())(any(), any()))
        .thenReturn(Future.successful(Some(AssetMonetaryAmount(validAnswer))))

      val request =
        FakeRequest(POST, assetMoneyValueRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[AssetMoneyValueView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, NormalMode)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, assetMoneyValueRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, assetMoneyValueRoute)
          .withFormUrlEncodedBody(("value", validAnswer.toString))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
