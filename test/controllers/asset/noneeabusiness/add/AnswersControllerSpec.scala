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
import models.assets.{AssetMonetaryAmount, Assets}
import models.{NonUkAddress, WhatKindOfAsset, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.asset.WhatKindOfAssetPage
import pages.asset.noneeabusiness._
import pages.asset.noneeabusiness.add.StartDatePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.NonEeaBusinessPrintHelper
import views.html.asset.noneeabusiness.add.AnswersView
import mapping.NonEeaBusinessAssetMapper

import java.time.LocalDate
import scala.concurrent.Future

class AnswersControllerSpec extends SpecBase {

  private val name = "Noneeabusiness"

  private def ua(migrating: Boolean): UserAnswers =
    emptyUserAnswers.copy(isMigratingToTaxable = migrating)
      .set(WhatKindOfAssetPage(index), WhatKindOfAsset.NonEeaBusiness).success.value
      .set(NamePage(index), name).success.value
      .set(NonUkAddressPage(index), NonUkAddress("Line 1", "Line 2", Some("Line 3"), "FR")).success.value
      .set(GoverningCountryPage(index), "FR").success.value
      .set(StartDatePage(index), LocalDate.parse("1996-02-03")).success.value

  private lazy val getRoute  = routes.AnswersController.onPageLoad(index).url
  private lazy val postRoute = routes.AnswersController.onSubmit(index).url

  "AnswersController (non-EEA add)" must {

    "return OK and the correct view for a GET" in {
      val answers = ua(migrating = false)

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, getRoute)
      val result  = route(application, request).value

      val view         = application.injector.instanceOf[AnswersView]
      val printHelper  = application.injector.instanceOf[NonEeaBusinessPrintHelper]
      val answerSection = printHelper(answers, index, provisional = true, name)

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(index, answerSection)(request, messages).toString

      application.stop()
    }

    "redirect to AddAssets page when valid data is submitted and not migrating" in {
      val mockConnector = mock[TrustsConnector]
      val answers       = ua(migrating = false)

      val moneyAsset = AssetMonetaryAmount(4000L)
      val assets     = Assets(monetary = List(moneyAsset))

      when(mockConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(assets))

      // Amend succeeds -> controller takes OK/NO_CONTENT branch
      when(mockConnector.amendNonEeaBusinessAsset(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      // Not used when amend succeeds, but safe to stub:
      when(mockConnector.addNonEeaBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

      val request = FakeRequest(POST, postRoute)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual
        controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "redirect to AddAssets page when valid data is submitted and migrating to taxable" in {
      val mockConnector = mock[TrustsConnector]
      val answers       = ua(migrating = true)

      val moneyAsset = AssetMonetaryAmount(4000L)
      val assets     = Assets(monetary = List(moneyAsset))

      when(mockConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(assets))

      // Amend succeeds -> controller takes OK/NO_CONTENT branch
      when(mockConnector.amendNonEeaBusinessAsset(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      when(mockConnector.addNonEeaBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

      val request = FakeRequest(POST, postRoute)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual
        controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, getRoute)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, postRoute)
      val result  = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "return an internal server error when the mapper returns None" in {
      val mockMapper = mock[NonEeaBusinessAssetMapper]

      val application =
        applicationBuilder(userAnswers = Some(ua(migrating = true)))
          .overrides(bind[NonEeaBusinessAssetMapper].toInstance(mockMapper))
          .build()

      when(mockMapper(any())).thenReturn(None)

      val request = FakeRequest(POST, postRoute)
      val result  = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }
  }
}
