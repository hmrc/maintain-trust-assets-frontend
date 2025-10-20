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

package controllers.asset.business.remove

import base.SpecBase
import connectors.TrustsConnector
import forms.RemoveIndexFormProvider
import models.assets._
import models.{NonUkAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.asset.business.remove.RemoveBusinessAssetYesNoView

import scala.concurrent.Future

class RemoveBusinessAssetYesNoControllerSpec extends SpecBase with ScalaCheckPropertyChecks with ScalaFutures {

  lazy val formProvider = new RemoveIndexFormProvider()
  lazy val form: Form[Boolean] = formProvider(messagesPrefix)
  lazy val formRoute: Call = routes.RemoveBusinessAssetYesNoController.onSubmit(0)
  val messagesPrefix = "business.removeYesNo"
  val mockConnector: TrustsConnector = mock[TrustsConnector]
  val businessAssets: List[BusinessAssetType] = List(
    createAsset(0, provisional = false),
    createAsset(1, provisional = true),
    createAsset(2, provisional = true)
  )

  def createAsset(id: Int, provisional: Boolean): BusinessAssetType =
    BusinessAssetType(s"Business Name $id", "", NonUkAddress("", "", None, ""), 123L)

  def userAnswers(migrating: Boolean): UserAnswers = emptyUserAnswers.copy(isMigratingToTaxable = migrating)

  "RemoveBusinessAssetYesNoController" when {

    "return OK and the correct view for a GET" in {


      when(mockConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(Assets(Nil, Nil, Nil, businessAssets, Nil, Nil, Nil)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustsConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveBusinessAssetYesNoController.onPageLoad(index).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemoveBusinessAssetYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, index, s"Business Name $index")(request, messages).toString

      application.stop()
    }

    "gets exception when service fails and it will be redirected" in {

      when(mockConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.failed(new IndexOutOfBoundsException("No asset found")))

      val answers = userAnswers(migrating = true)

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveBusinessAssetYesNoController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }


    "return INTERNAL_SERVER_ERROR when service fails" in {

      when(mockConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.failed(new Throwable("failed")))

      val answers = userAnswers(migrating = true)

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveBusinessAssetYesNoController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

    "not removing the asset" must {

      "redirect to the 'add asset' page when valid data is submitted and migrating" in {

        val answers = userAnswers(migrating = true)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveBusinessAssetYesNoController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

        application.stop()
      }
    }

    "removing an old asset" must {

      "redirect to the 'add asset' page, removing the asset when migrating" in {

        val answers = userAnswers(migrating = true)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        when(mockConnector.getAssets(any())(any(), any()))
          .thenReturn(Future.successful(Assets(Nil, Nil, Nil, businessAssets, Nil, Nil, Nil)))

        when(mockConnector.removeAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))

        val request =
          FakeRequest(POST, routes.RemoveBusinessAssetYesNoController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(bind[TrustsConnector].toInstance(mockConnector)).build()

      val request =
        FakeRequest(POST, routes.RemoveBusinessAssetYesNoController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[RemoveBusinessAssetYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, s"Business Name $index")(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.RemoveBusinessAssetYesNoController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, routes.RemoveBusinessAssetYesNoController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
