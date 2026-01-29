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

package controllers.asset.business.add

import base.SpecBase
import connectors.TrustsConnector
import controllers.routes._
import mapping.BusinessAssetMapper
import models.{UkAddress, UserAnswers}
import models.assets.{Assets, BusinessAssetType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.asset.business._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.BusinessPrintHelper
import views.html.asset.business.add.BusinessAnswersView

import scala.concurrent.Future

class BusinessAnswersControllerSpec extends SpecBase with MockitoSugar {

  private val name: String = "Business"

  private val answers: UserAnswers = emptyUserAnswers
    .set(BusinessNamePage(index), name)
    .success
    .value
    .set(BusinessDescriptionPage(index), "test test test")
    .success
    .value
    .set(BusinessAddressUkYesNoPage(index), true)
    .success
    .value
    .set(BusinessUkAddressPage(index), UkAddress("Line 1", "Line 2", None, None, "NE11NE"))
    .success
    .value
    .set(BusinessValuePage(index), 12L)
    .success
    .value

  private def businessExisting(
    org: String = name,
    desc: String = "test test test",
    line1: String = "Line 1",
    line2: String = "Line 2",
    postcode: String = "NE11NE",
    value: Long = 12L
  ): BusinessAssetType =
    BusinessAssetType(
      orgName = org,
      businessDescription = desc,
      address = UkAddress(line1, line2, None, None, postcode),
      businessValue = value
    )

  "BusinessAnswersController" must {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, routes.BusinessAnswersController.onPageLoad(index).url)

      val result = route(application, request).value

      val view          = application.injector.instanceOf[BusinessAnswersView]
      val printHelper   = application.injector.instanceOf[BusinessPrintHelper]
      val answerSection = printHelper(answers, index, provisional = true, name)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(index, answerSection)(request, messages).toString

      application.stop()
    }

    "redirect to the next page when valid data is submitted (add path success)" in {
      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
        .build()

      when(mockTrustConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(Assets()))

      when(mockTrustConnector.amendBusinessAsset(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      when(mockTrustConnector.addBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController
        .onPageLoad()
        .url

      application.stop()
    }

    "redirect via amend path when upstream indicates existing record (amend success)" in {
      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
        .build()

      val existing = businessExisting()
      when(mockTrustConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(Assets(business = List(existing))))

      when(mockTrustConnector.amendBusinessAsset(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit(index).url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER

      application.stop()
    }

    "return INTERNAL_SERVER_ERROR when amend returns non-2xx" in {
      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
        .build()

      val existing = businessExisting()
      when(mockTrustConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(Assets(business = List(existing))))

      when(mockTrustConnector.amendBusinessAsset(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "bad amend")))

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit(index).url)
      val result  = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

    "return INTERNAL_SERVER_ERROR when add returns non-2xx" in {
      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
        .build()

      when(mockTrustConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(Assets())) // add path

      when(mockTrustConnector.addBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(BAD_GATEWAY, "bad add")))

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit(index).url)
      val result  = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

    "skip add (duplicate exists) and redirect immediately" in {
      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
        .build()

      val matching  = businessExisting()
      val different = matching.copy(orgName = "Other Ltd")
      when(mockTrustConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(Assets(business = List(different, matching))))

      when(mockTrustConnector.addBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))
      when(mockTrustConnector.amendBusinessAsset(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit(index).url)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER

      application.stop()
    }

    "return INTERNAL_SERVER_ERROR when mapper returns None" in {
      val mockMapper = mock[BusinessAssetMapper]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[BusinessAssetMapper].toInstance(mockMapper))
        .build()

      when(mockMapper(any)).thenReturn(None)

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit(index).url)
      val result  = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.BusinessAnswersController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, routes.BusinessAnswersController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

}
