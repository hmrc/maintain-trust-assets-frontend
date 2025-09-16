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

package controllers.asset.partnership.amend

import base.SpecBase
import connectors.TrustsConnector
import controllers.routes._
import models.WhatKindOfAsset.Partnership
import models.assets.PartnershipType
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.asset.WhatKindOfAssetPage
import pages.asset.partnership.{PartnershipDescriptionPage, PartnershipStartDatePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HttpResponse
import utils.print.PartnershipPrintHelper
import views.html.asset.partnership.PartnershipAmendAnswersView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class PartnershipAmendAnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private lazy val answersRoute = routes.PartnershipAmendAnswersController.extractAndRender(index).url
  private lazy val submitAnswersRoute = routes.PartnershipAmendAnswersController.onSubmit(index).url

  val validDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
  val name: String = "Description"

  private val partnershipType = PartnershipType(name, validDate)

  val answers =
    emptyUserAnswers
      .set(WhatKindOfAssetPage(index), Partnership).success.value
      .set(PartnershipDescriptionPage(index), name).success.value
      .set(PartnershipStartDatePage(index), validDate).success.value

  "BusinessAmendAnswersController" must {

    "return OK and the correct view for a GET for a given index" in {

      val mockService: TrustService = mock[TrustService]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()


      when(mockService.getPartnershipAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(partnershipType))


      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[PartnershipAmendAnswersView]
      val printHelper = application.injector.instanceOf[PartnershipPrintHelper]
      val answerSection = printHelper(answers, index, provisional = false, name)


      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(answerSection, index)(request, messages).toString
    }


    "return INTERNAL_SERVER_ERROR when service fails" in {

      val mockService = mock[TrustService]

      val application = applicationBuilder(Some(answers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()

      when(mockService.getPartnershipAsset(any(), any())(any(), any()))
        .thenReturn(Future.failed(new Exception("failed")))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      status(result) mustEqual INTERNAL_SERVER_ERROR

    }


    "redirect to the 'add asset' page when submitted and migrating to taxable" in {

      val mockTrustConnector = mock[TrustsConnector]

      val application =
        applicationBuilder(userAnswers = Some(answers), affinityGroup = Agent)
          .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
          .build()

      when(mockTrustConnector.amendPartnershipAsset(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

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
