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

package controllers.asset.nonTaxableToTaxable

import base.SpecBase
import connectors.TrustsStoreConnector
import controllers.IndexValidation
import controllers.routes._
import forms.YesNoFormProvider
import models.TaskStatus._
import models.assets.NonEeaBusinessType
import models.{UkAddress, UserAnswers}
import navigation.AssetsNavigator
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import pages.asset.nontaxabletotaxable.AddAssetsYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.HttpResponse
import views.html.asset.nonTaxableToTaxable.AddAssetYesNoView

import java.time.LocalDate
import scala.concurrent.Future

class AddAssetYesNoControllerSpec extends SpecBase with IndexValidation with ScalaFutures with BeforeAndAfterEach {

  private val form: Form[Boolean] = new YesNoFormProvider().withPrefix("nonTaxableToTaxable.addAssetYesNo")

  lazy val addAssetYesNoRoute: String =
    controllers.asset.nonTaxableToTaxable.routes.AddAssetYesNoController.onPageLoad().url

  lazy val addAssetYesNoPostRoute: String =
    controllers.asset.nonTaxableToTaxable.routes.AddAssetYesNoController.onSubmit().url

  private val mockTrustService: TrustService                = mock[TrustService]
  private val mockTrustStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
  private val mockNavigator: AssetsNavigator                = mock[AssetsNavigator]

  override def emptyUserAnswers: UserAnswers = super.emptyUserAnswers.copy(isMigratingToTaxable = true)

  override def beforeEach(): Unit = {
    reset(mockNavigator)
    reset(mockTrustService)
    reset(mockTrustStoreConnector)

    when(mockNavigator.redirectFromAddAssetYesNoPage(any(), any(), any())).thenReturn(fakeNavigator.desiredRoute)

    when(mockTrustStoreConnector.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, "")))
  }

  "AddAssetYesNo Controller" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, addAssetYesNoRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AddAssetYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form)(request, messages).toString

      application.stop()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(AddAssetsYesNoPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, addAssetYesNoRoute)

      val view = application.injector.instanceOf[AddAssetYesNoView]

      val result = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form.fill(true))(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted, when no assets and not adding any more" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AssetsNavigator].toInstance(mockNavigator))
        .overrides(bind[TrustService].toInstance(mockTrustService))
        .overrides(bind[TrustsStoreConnector].toInstance(mockTrustStoreConnector))
        .build()

      when(mockTrustService.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(models.assets.Assets()))

      val request = FakeRequest(POST, addAssetYesNoPostRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      verify(mockNavigator).redirectFromAddAssetYesNoPage(value = false, isMigratingToTaxable = true, noAssets = true)

      verify(mockTrustStoreConnector).updateTaskStatus(any(), eqTo(InProgress))(any(), any())

      application.stop()
    }

    "redirect to the next page when valid data is submitted, when no assets and adding more" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AssetsNavigator].toInstance(mockNavigator))
        .overrides(bind[TrustService].toInstance(mockTrustService))
        .overrides(bind[TrustsStoreConnector].toInstance(mockTrustStoreConnector))
        .build()

      when(mockTrustService.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(models.assets.Assets()))

      val request = FakeRequest(POST, addAssetYesNoPostRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      verify(mockNavigator).redirectFromAddAssetYesNoPage(value = true, isMigratingToTaxable = true, noAssets = true)

      verify(mockTrustStoreConnector, never()).updateTaskStatus(any(), any())(any(), any())

      application.stop()
    }

    "redirect to the next page when valid data is submitted, when has non-eea asset and adding no more" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AssetsNavigator].toInstance(mockNavigator))
        .overrides(bind[TrustService].toInstance(mockTrustService))
        .overrides(bind[TrustsStoreConnector].toInstance(mockTrustStoreConnector))
        .build()

      val nonEEACompanies = List(
        NonEeaBusinessType(
          None,
          "Non-eea Business",
          address = UkAddress("line 1", "line 2", None, None, "ne981zz"),
          "GB",
          LocalDate.now,
          None,
          provisional = false
        )
      )

      when(mockTrustService.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(models.assets.Assets(nonEEABusiness = nonEEACompanies)))

      val request = FakeRequest(POST, addAssetYesNoPostRoute)
        .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

      verify(mockNavigator).redirectFromAddAssetYesNoPage(value = false, isMigratingToTaxable = true, noAssets = false)

      verify(mockTrustStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val mockTrustService: TrustService = mock[TrustService]

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustService].toInstance(mockTrustService))
        .build()

      when(mockTrustService.getAssets(any())(any(), any())).thenReturn(Future.successful(models.assets.Assets()))

      val request = FakeRequest(POST, addAssetYesNoPostRoute)
        .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[AddAssetYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm)(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, addAssetYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, addAssetYesNoPostRoute)
        .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

}
