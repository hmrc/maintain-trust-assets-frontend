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

package controllers.asset.noneeabusiness.amend

import base.SpecBase
import connectors.TrustsConnector
import mapping.NonEeaBusinessAssetMapper
import models.assets.{Assets, NonEeaBusinessType}
import models.{NonUkAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.asset.noneeabusiness.add.StartDatePage
import pages.asset.noneeabusiness.amend.IndexPage
import pages.asset.noneeabusiness.{GoverningCountryPage, NamePage, NonUkAddressPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HttpResponse
import utils.print.NonEeaBusinessPrintHelper
import views.html.asset.noneeabusiness.amend.AnswersView

import java.time.LocalDate
import scala.concurrent.Future

class AnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private lazy val answersRoute =
    controllers.asset.noneeabusiness.amend.routes.AnswersController.extractAndRender(index).url

  private lazy val submitAnswersRoute =
    controllers.asset.noneeabusiness.amend.routes.AnswersController.onSubmit(index).url

  private lazy val renderFromUaRoute =
    controllers.asset.noneeabusiness.amend.routes.AnswersController.renderFromUserAnswers(index).url

  private val name: String               = "OrgName"
  private val date: LocalDate            = LocalDate.parse("1996-02-03")
  private val country: String            = "FR"
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "FR")

  private val nonEeaBusinessAsset = NonEeaBusinessType(
    lineNo = None,
    orgName = name,
    address = nonUkAddress,
    govLawCountry = country,
    startDate = date,
    endDate = None,
    provisional = true
  )

  val assets: Assets = Assets(
    nonEEABusiness = List(nonEeaBusinessAsset)
  )

  def userAnswers(migrating: Boolean): UserAnswers = emptyUserAnswers
    .copy(isMigratingToTaxable = migrating)
    .set(NamePage(index), name)
    .success
    .value
    .set(IndexPage, index)
    .success
    .value
    .set(NonUkAddressPage(index), nonUkAddress)
    .success
    .value
    .set(GoverningCountryPage(index), country)
    .success
    .value
    .set(StartDatePage(index), date)
    .success
    .value

  "Answers Controller" must {

    "return OK and the correct view for a GET for a given index" in {

      val mockService: TrustService = mock[TrustService]

      val answers = userAnswers(migrating = false)

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()

      when(mockService.getNonEeaBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(nonEeaBusinessAsset))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      val view          = application.injector.instanceOf[AnswersView]
      val printHelper   = application.injector.instanceOf[NonEeaBusinessPrintHelper]
      val answerSection = printHelper(answers, index, provisional = false, name)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answerSection, index)(request, messages).toString

      application.stop()
    }

    "render from existing user answers" in {

      val answers = userAnswers(migrating = false)

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, renderFromUaRoute)

      val result = route(application, request).value

      status(result) mustEqual OK

      application.stop()
    }

    "return INTERNAL_SERVER_ERROR when extract fails" in {

      val failingService: TrustService = mock[TrustService]
      val answers                      = userAnswers(migrating = false)

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[TrustService].toInstance(failingService)
        )
        .build()

      when(failingService.getNonEeaBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }

    "redirect after successful submit" in {

      val mockTrustConnector = mock[TrustsConnector]
      val answers            = userAnswers(migrating = true)

      val application =
        applicationBuilder(userAnswers = Some(answers), affinityGroup = Agent)
          .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
          .build()

      when(mockTrustConnector.amendNonEeaBusinessAsset(any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController
        .onPageLoad()
        .url

      application.stop()
    }

    "return INTERNAL_SERVER_ERROR when mapper returns None" in {

      val mockMapper = mock[NonEeaBusinessAssetMapper]
      val answers    = userAnswers(migrating = false)

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[NonEeaBusinessAssetMapper].toInstance(mockMapper))
          .build()

      when(mockMapper(any)).thenReturn(None)

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

      application.stop()
    }
  }

}
