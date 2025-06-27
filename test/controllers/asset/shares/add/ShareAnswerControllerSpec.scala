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

package controllers.asset.shares.add

import base.SpecBase
import connectors.TrustsConnector
import models.{ShareClass, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import pages.asset.shares._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.SharesPrintHelper
import views.html.asset.shares.add.ShareAnswersView

import scala.concurrent.Future

class ShareAnswerControllerSpec extends SpecBase {

  private lazy val shareAnswerRoute: String = routes.ShareAnswerController.onPageLoad(index).url

  val name: String = "OrgName"
  val assetValue: Long = 300L
  val quantity: Long = 20L

  val answers: UserAnswers = emptyUserAnswers
    .set(SharesInAPortfolioPage(index), true).success.value
    .set(SharePortfolioNamePage(index), name).success.value
    .set(SharePortfolioQuantityInTrustPage(index), quantity).success.value
    .set(ShareClassPage(index), ShareClass.Other).success.value
    .set(SharePortfolioOnStockExchangePage(index), false).success.value
    .set(SharePortfolioValueInTrustPage(index), assetValue).success.value

  "ShareAnswer Controller" must {

    "return OK and the correct view for a GET" when {

      "share company name" in {

        val name: String = "Company Name"

        val answers = emptyUserAnswers
          .set(SharesInAPortfolioPage(index), false).success.value
          .set(ShareCompanyNamePage(index), name).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, shareAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ShareAnswersView]
        val printHelper = application.injector.instanceOf[SharesPrintHelper]
        val answerSection = printHelper(answers, index, provisional = true, name)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, answerSection)(request, messages).toString

        application.stop()
      }

      "portfolio name" in {

        val name: String = "Portfolio Name"

        val answers = emptyUserAnswers
          .set(SharesInAPortfolioPage(index), true).success.value
          .set(SharePortfolioNamePage(index), name).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, shareAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ShareAnswersView]
        val printHelper = application.injector.instanceOf[SharesPrintHelper]
        val answerSection = printHelper(answers, index, provisional = true, name)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, answerSection)(request, messages).toString

        application.stop()
      }

      "no name" in {

        val answers = emptyUserAnswers
          .set(SharesInAPortfolioPage(index), true).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, shareAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ShareAnswersView]
        val printHelper = application.injector.instanceOf[SharesPrintHelper]
        val answerSection = printHelper(answers, index, provisional = true, "name") //TODO

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, answerSection)(request, messages).toString

        application.stop()
      }
    }

    "redirect to the next page when valid data is submitted" in {
      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
        .build()

      when(mockTrustConnector.addSharesAsset(eqTo(index), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, routes.ShareAnswerController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, shareAnswerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

  }
}
