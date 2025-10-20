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

package controllers.asset.property_or_land.amend

import base.SpecBase
import connectors.TrustsConnector
import models.UserAnswers
import models.assets.PropertyLandType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.asset.property_or_land._
import pages.asset.property_or_land.amend.IndexPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HttpResponse
import utils.print.PropertyOrLandPrintHelper
import views.html.asset.property_or_land.amend.AnswersView

import scala.concurrent.Future

class PropertyOrLandAmendAnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private lazy val answersRoute = routes.PropertyOrLandAmendAnswersController.extractAndRender(index).url
  private lazy val submitAnswersRoute = routes.PropertyOrLandAmendAnswersController.onSubmit(index).url

  private val name: String = "BusinessName"
  private val valueFull: Long = 790L

  private val propertyOrLandAsset = PropertyLandType(
    buildingLandName = Some(name),
    address = None,
    valueFull = valueFull,
    valuePrevious = None
  )

  def userAnswers: UserAnswers = emptyUserAnswers.copy(isMigratingToTaxable = true)
    .set(IndexPage, index).success.value
    .set(PropertyOrLandAddressYesNoPage(index), false).success.value
    .set(PropertyOrLandDescriptionPage(index), name).success.value
    .set(PropertyOrLandTotalValuePage(index), valueFull).success.value
    .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value


  "PropertyOrLandAmendAnswersController" must {

    "return OK and the correct view for a GET for a given index" in {

      val mockService: TrustService = mock[TrustService]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()

      when(mockService.getPropertyOrLandAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(propertyOrLandAsset))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AnswersView]
      val printHelper = application.injector.instanceOf[PropertyOrLandPrintHelper]
      val answerSection = printHelper(userAnswers, index, provisional = false, name)

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answerSection, index)(request, messages).toString
    }


    "return INTERNAL_SERVER_ERROR when service fails" in {

      val mockService: TrustService = mock[TrustService]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()

      when(mockService.getPropertyOrLandAsset(any(), any())(any(), any()))
        .thenReturn(Future.failed(new Exception("failed")))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR
    }

    "redirect to the 'add asset' page when submitted and migrating to taxable" in {

      val mockTrustConnector = mock[TrustsConnector]

      val application =
        applicationBuilder(userAnswers = Some(userAnswers), affinityGroup = Agent)
          .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
          .build()

      when(mockTrustConnector.amendPropertyOrLandAsset(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

  }
}
