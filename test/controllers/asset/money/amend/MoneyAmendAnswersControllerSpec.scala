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

package controllers.asset.money.amend

import base.SpecBase
import connectors.TrustsConnector
import controllers.routes._
import models.UserAnswers
import models.assets.AssetMonetaryAmount
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.asset.money.AssetMoneyValuePage
import pages.asset.money.amend.IndexPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HttpResponse
import utils.print.MoneyPrintHelper
import views.html.OutOfBoundsPageNotFoundView
import views.html.asset.money.amend.MoneyAmendAnswersView

import scala.concurrent.Future

class MoneyAmendAnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private lazy val answersRoute =
    controllers.asset.money.amend.routes.MoneyAmendAnswersController.extractAndRender(index).url

  private lazy val submitAnswersRoute =
    controllers.asset.money.amend.routes.MoneyAmendAnswersController.onSubmit(index).url

  private val assetValue: Long = 4000L
  private val name: String     = assetValue.toString

  private val moneyAsset = AssetMonetaryAmount(assetValue)

  private val userAnswers: UserAnswers =
    emptyUserAnswers
      .set(IndexPage, index)
      .success
      .value
      .set(AssetMoneyValuePage(index), assetValue)
      .success
      .value

  "MoneyAmendAnswersController" must {

    "return OK and the correct view for a GET for a given index" in {
      val mockService: TrustService = mock[TrustService]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()

      when(mockService.getMonetaryAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(moneyAsset))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[MoneyAmendAnswersView]

      val printHelper = application.injector.instanceOf[MoneyPrintHelper]

      val answerSection = printHelper(userAnswers, index, provisional = false, name)

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(answerSection, index)(request, messages).toString

      application.stop()
    }

    "return INTERNAL_SERVER_ERROR when service fails" in {
      val mockService: TrustService = mock[TrustService]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()

      when(mockService.getMonetaryAsset(any(), any())(any(), any()))
        .thenReturn(Future.failed(new Exception("failed")))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

    "return Not Found and the out of bounds page when getMonetaryAsset throws IndexOutOfBoundsException" in {
      val mockService: TrustService = mock[TrustService]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()

      when(mockService.getMonetaryAsset(any(), any())(any(), any()))
        .thenReturn(Future.failed(new IndexOutOfBoundsException("")))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[OutOfBoundsPageNotFoundView]

      status(result) mustEqual NOT_FOUND

      contentAsString(result) mustEqual view(isMigratingToTaxable = false)(request, messages).toString

      application.stop()
    }

    "redirect to the 'add asset' page when submitted and migrating to taxable" in {
      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(userAnswers), affinityGroup = Agent)
        .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
        .build()

      when(mockTrustConnector.amendMoneyAsset(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController
        .onPageLoad()
        .url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }

}
