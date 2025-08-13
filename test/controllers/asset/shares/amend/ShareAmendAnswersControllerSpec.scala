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

package controllers.asset.shares.amend

import base.SpecBase
import connectors.TrustsConnector
import models.assets.SharesType
import models.{ShareClass, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.asset.shares._
import pages.asset.shares.amend.IndexPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HttpResponse
import utils.Constants.UNQUOTED
import utils.print.SharesPrintHelper
import views.html.asset.shares.amend.ShareAmendAnswersView

import scala.concurrent.Future

class ShareAmendAnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private lazy val answersRoute = routes.ShareAmendAnswersController.extractAndRender(index).url
  private lazy val submitAnswersRoute = routes.ShareAmendAnswersController.onSubmit(index).url

    private val name: String = "ShareName"
  private val quantity: Long = 5
  private val assetValue: Long = 790L

  private val shareAsset = SharesType(
    numberOfShares = quantity.toString,
    orgName = name,
    shareClass = ShareClass.toDES(ShareClass.Deferred),
    typeOfShare = UNQUOTED,
    value = assetValue,
    isPortfolio = Some(true)
  )

  private val userAnswers: UserAnswers = emptyUserAnswers
    .set(IndexPage, index).success.value
    .set(SharesInAPortfolioPage(index), true).success.value
    .set(SharePortfolioNamePage(index), name).success.value
    .set(SharePortfolioQuantityInTrustPage(index), quantity).success.value
    .set(ShareClassPage(index), ShareClass.Deferred).success.value
    .set(SharePortfolioOnStockExchangePage(index), false).success.value
    .set(SharePortfolioValueInTrustPage(index), assetValue).success.value

  "ShareAmendAnswersController" must {

    "return OK and the correct view for a GET for a given index" in {

      val mockService: TrustService = mock[TrustService]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()

      when(mockService.getSharesAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(shareAsset))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ShareAmendAnswersView]
      val printHelper = application.injector.instanceOf[SharesPrintHelper]
      val answerSection = printHelper(userAnswers, index, provisional = false, name)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answerSection, index)(request, messages).toString
    }

    "redirect to the 'add asset' page when submitted and migrating to taxable" in {

      val mockTrustConnector = mock[TrustsConnector]

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), affinityGroup = Agent)
          .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
          .build()

      when(mockTrustConnector.amendSharesAsset(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }
  }
}
