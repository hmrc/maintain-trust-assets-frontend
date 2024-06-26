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

package controllers.asset.noneeabusiness.amend

import base.SpecBase
import connectors.TrustsConnector
import models.{NonUkAddress, UserAnswers}
import models.assets.NonEeaBusinessType
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

  private lazy val answersRoute = routes.AnswersController.extractAndRender(index).url
  private lazy val submitAnswersRoute = routes.AnswersController.onSubmit(index).url

  private val index = 0
  private val name: String = "OrgName"
  private val date: LocalDate = LocalDate.parse("1996-02-03")
  private val country: String = "FR"
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

  def userAnswers(migrating: Boolean): UserAnswers = emptyUserAnswers.copy(isMigratingToTaxable = migrating)
    .set(NamePage, name).success.value
    .set(IndexPage, index).success.value
    .set(NonUkAddressPage, nonUkAddress).success.value
    .set(GoverningCountryPage, country).success.value
    .set(StartDatePage, date).success.value

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

      val view = application.injector.instanceOf[AnswersView]
      val printHelper = application.injector.instanceOf[NonEeaBusinessPrintHelper]
      val answerSection = printHelper(answers, provisional = false, name)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answerSection, index)(request, messages).toString
    }

    "redirect to the 'add non-eea asset' page when submitted and not migrating" in {

      val mockTrustConnector = mock[TrustsConnector]

      val answers = userAnswers(migrating = false)

      val application =
        applicationBuilder(userAnswers = Some(answers), affinityGroup = Agent)
          .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
          .build()

      when(mockTrustConnector.amendNonEeaBusinessAsset(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad().url

      application.stop()
    }

    "redirect to the 'add asset' page when submitted and migrating to taxable" in {

      val mockTrustConnector = mock[TrustsConnector]

      val answers = userAnswers(migrating = true)

      val application =
        applicationBuilder(userAnswers = Some(answers), affinityGroup = Agent)
          .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
          .build()

      when(mockTrustConnector.amendNonEeaBusinessAsset(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

  }
}
