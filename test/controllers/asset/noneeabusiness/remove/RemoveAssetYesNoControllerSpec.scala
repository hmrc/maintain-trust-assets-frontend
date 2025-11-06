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

package controllers.asset.noneeabusiness.remove

import base.SpecBase
import forms.RemoveIndexFormProvider
import models.{NonUkAddress, UserAnswers}
import models.assets._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.HttpResponse
import views.html.asset.noneeabusiness.remove.RemoveAssetYesNoView

import java.time.LocalDate
import scala.concurrent.Future

class RemoveAssetYesNoControllerSpec extends SpecBase with ScalaCheckPropertyChecks with ScalaFutures {

  val messagesPrefix = "nonEeaBusiness.removeYesNo"

  lazy val formProvider = new RemoveIndexFormProvider()
  lazy val form: Form[Boolean] = formProvider(messagesPrefix)

  lazy val formRoute: Call = routes.RemoveAssetYesNoController.onSubmit(0)

  private def createAsset(id: Int, provisional: Boolean): NonEeaBusinessType =
    NonEeaBusinessType(None, s"OrgName $id", NonUkAddress("L1", "L2", Some("L3"), "FR"), "FR", LocalDate.now, None, provisional)

  private def userAnswers(migrating: Boolean): UserAnswers =
    emptyUserAnswers.copy(isMigratingToTaxable = migrating)

  "RemoveAssetYesNo Controller" when {

    "return OK and the correct view for a GET" in {

      val mockTrustService: TrustService = mock[TrustService]

      when(mockTrustService.getNonEeaBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(createAsset(index, provisional = true)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustService].toInstance(mockTrustService))
        .build()

      val request = FakeRequest(GET, routes.RemoveAssetYesNoController.onPageLoad(index).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemoveAssetYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, index, s"OrgName $index")(request, messages).toString

      application.stop()
    }

    "redirect to add-asset page on GET when index is out of bounds" in {

      val mockTrustService: TrustService = mock[TrustService]

      when(mockTrustService.getNonEeaBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.failed(new IndexOutOfBoundsException("boom")))

      val answers = userAnswers(migrating = false)

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustService].toInstance(mockTrustService))
        .build()

      val request = FakeRequest(GET, routes.RemoveAssetYesNoController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad().url

      application.stop()
    }

    "return INTERNAL_SERVER_ERROR on GET when service fails unexpectedly" in {

      val mockTrustService: TrustService = mock[TrustService]

      when(mockTrustService.getNonEeaBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.failed(new RuntimeException("unexpected")))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustService].toInstance(mockTrustService))
        .build()

      val request = FakeRequest(GET, routes.RemoveAssetYesNoController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

    "not removing the asset" must {

      "redirect to the 'add non-eea asset' page when valid data is submitted and not migrating" in {

        val mockTrustService: TrustService = mock[TrustService]

        val answers = userAnswers(migrating = false)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustService].toInstance(mockTrustService))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveAssetYesNoController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad().url

        application.stop()
      }

      "redirect to the 'add asset' page when valid data is submitted and migrating" in {

        val mockTrustService: TrustService = mock[TrustService]

        val answers = userAnswers(migrating = true)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustService].toInstance(mockTrustService))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveAssetYesNoController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

        application.stop()
      }
    }

    "removing a new asset" must {

      "redirect to the 'add non-eea asset' page, removing the asset when not migrating (provisional = true)" in {

        val mockTrustService: TrustService = mock[TrustService]

        val i = 2
        val answers = userAnswers(migrating = false)

        when(mockTrustService.getNonEeaBusinessAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(createAsset(i, provisional = true)))

        when(mockTrustService.removeAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustService].toInstance(mockTrustService))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveAssetYesNoController.onSubmit(i).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad().url

        verify(mockTrustService, times(1)).removeAsset(any(), any())(any(), any())

        application.stop()
      }

      "redirect to the 'add asset' page, removing the asset when migrating (provisional = true)" in {

        val mockTrustService: TrustService = mock[TrustService]

        val i = 1
        val answers = userAnswers(migrating = true)

        when(mockTrustService.getNonEeaBusinessAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(createAsset(i, provisional = true)))

        when(mockTrustService.removeAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustService].toInstance(mockTrustService))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveAssetYesNoController.onSubmit(i).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

        verify(mockTrustService, times(1)).removeAsset(any(), any())(any(), any())

        application.stop()
      }
    }

    "removing an old asset" must {

      "redirect to the end date when provisional = false" in {

        val mockTrustService: TrustService = mock[TrustService]

        when(mockTrustService.getNonEeaBusinessAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(createAsset(index, provisional = false)))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[TrustService].toInstance(mockTrustService))
          .build()

        val request =
          FakeRequest(POST, routes.RemoveAssetYesNoController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.noneeabusiness.remove.routes.RemoveAssetEndDateController.onPageLoad(index).url

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val mockTrustService: TrustService = mock[TrustService]

      when(mockTrustService.getNonEeaBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(createAsset(index, provisional = true)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustService].toInstance(mockTrustService))
        .build()

      val request =
        FakeRequest(POST, routes.RemoveAssetYesNoController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[RemoveAssetYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, s"OrgName $index")(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.RemoveAssetYesNoController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, routes.RemoveAssetYesNoController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
