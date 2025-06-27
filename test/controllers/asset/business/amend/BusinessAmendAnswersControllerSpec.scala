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

package controllers.asset.business.amend

import base.SpecBase
import connectors.TrustsConnector
import models.assets.BusinessAssetType
import models.{NonUkAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.asset.business._
import pages.asset.business.amend.IndexPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HttpResponse
import utils.print.BusinessPrintHelper
import views.html.asset.business.amend.BusinessAmendAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class BusinessAmendAnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private lazy val answersRoute = routes.BusinessAmendAnswersController.extractAndRender(index).url
  private lazy val submitAnswersRoute = routes.BusinessAmendAnswersController.onSubmit(index).url

  private val name: String = "BusinessName"
  private val description: String = "BusinessDescription"
  private val internationalAddress: NonUkAddress = NonUkAddress("", "", None, "")
  private val valueFull: Long = 790L

  private val businessAsset = BusinessAssetType(
    orgName = name,
    businessDescription = description,
    address = internationalAddress,
    businessValue = valueFull
  )

  def userAnswers: UserAnswers = UserAnswers("internalId",
    "identifier",
    "sessionId",
    "internalId-identifier-sessionId",
    LocalDate.now,
    isMigratingToTaxable = true)
    .set(IndexPage, index).success.value
    .set(BusinessAddressUkYesNoPage(index), false).success.value
    .set(BusinessInternationalAddressPage(index), internationalAddress).success.value
    .set(BusinessNamePage(index), name).success.value
    .set(BusinessDescriptionPage(index), description).success.value
    .set(BusinessValuePage(index), valueFull).success.value


  "BusinessAmendAnswersController" must {

    "return OK and the correct view for a GET for a given index" in {

      val mockService: TrustService = mock[TrustService]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()

      when(mockService.getBusinessAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(businessAsset))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[BusinessAmendAnswersView]
      val printHelper = application.injector.instanceOf[BusinessPrintHelper]
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

      when(mockTrustConnector.amendBusinessAsset(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

  }
}
