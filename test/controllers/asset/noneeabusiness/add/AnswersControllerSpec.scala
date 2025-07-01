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

package controllers.asset.noneeabusiness.add

import base.SpecBase
import connectors.TrustsConnector
import controllers.routes._
import mapping.NonEeaBusinessAssetMapper
import models.assets.{AssetMonetaryAmount, Assets}
import models.{NonUkAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.asset.noneeabusiness._
import pages.asset.noneeabusiness.add.StartDatePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.NonEeaBusinessPrintHelper
import views.html.asset.noneeabusiness.add.AnswersView

import java.time.LocalDate
import scala.concurrent.Future

class AnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private val name: String = "Noneeabusiness"

  def userAnswers(migrating: Boolean): UserAnswers = emptyUserAnswers.copy(isMigratingToTaxable = migrating)
    .set(NamePage(index), name).success.value
    .set(NonUkAddressPage(index), NonUkAddress("Line 1", "Line 2", Some("Line 3"), "FR")).success.value
    .set(GoverningCountryPage(index), "FR").success.value
    .set(StartDatePage, LocalDate.parse("1996-02-03")).success.value

  private lazy val onPageLoadRoute: String = routes.AnswersController.onSubmit(index).url

  "AnswersController" must {

    "return OK and the correct view for a GET" in {

      val answers = userAnswers(migrating = false)

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AnswersView]

      val printHelper: NonEeaBusinessPrintHelper = application.injector.instanceOf[NonEeaBusinessPrintHelper]
      val answerSection = printHelper(answers, index, provisional = true, name)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(index, answerSection)(request, messages).toString

      application.stop()
    }

    "redirect to the add non-eea asset page when valid data is submitted and not migrating" in {
      val mockTrustsConnector = mock[TrustsConnector]

      val answers = userAnswers(migrating = false)

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustsConnector))
        .build()

      val moneyAsset = AssetMonetaryAmount(4000L)
      val assets: Assets = Assets(monetary = List(moneyAsset))
      when(mockTrustsConnector.getAssets(any())(any(), any())).thenReturn(Future.successful(assets))
      when(mockTrustsConnector.addNonEeaBusinessAsset( any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, routes.AnswersController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "redirect to the add non-eea asset page when valid data is submitted and are migrating to taxable" in {
      val mockTrustsConnector = mock[TrustsConnector]

      val answers = userAnswers(migrating = true)

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustsConnector))
        .build()

      val moneyAsset = AssetMonetaryAmount(4000L)
      val assets: Assets = Assets(monetary = List(moneyAsset))
      when(mockTrustsConnector.getAssets(any())(any(), any())).thenReturn(Future.successful(assets))
      when(mockTrustsConnector.addNonEeaBusinessAsset( any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, routes.AnswersController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, onPageLoadRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, routes.AnswersController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "return an internal server error given None is returned from NonEeaBusinessAssetMapper" in {

      val mockNonEeaBusinessAssetMapper = mock[NonEeaBusinessAssetMapper]

      val application =
        applicationBuilder(userAnswers = Some(userAnswers(migrating = true)))
          .overrides(bind[NonEeaBusinessAssetMapper].toInstance(mockNonEeaBusinessAssetMapper))
          .build()

      when(mockNonEeaBusinessAssetMapper(any)).thenReturn(None)

      val request = FakeRequest(POST, routes.AnswersController.onSubmit(index).url)

      val result = route(application, request).value
      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

  }
}
