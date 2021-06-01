/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.asset.other.amend

import java.time.LocalDate

import base.SpecBase
import connectors.TrustsConnector
import models.UserAnswers
import models.assets.OtherAssetType
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import pages.asset.other.amend.IndexPage
import pages.asset.other.{OtherAssetDescriptionPage, OtherAssetValuePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.auth.core.AffinityGroup.Agent
import uk.gov.hmrc.http.HttpResponse
import utils.print.OtherPrintHelper
import views.html.asset.other.amend.AnswersView

import scala.concurrent.Future

class AnswersControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  private lazy val answersRoute = controllers.asset.other.amend.routes.AnswersController.extractAndRender(index).url
  private lazy val submitAnswersRoute = controllers.asset.other.amend.routes.AnswersController.onSubmit(index).url

  private val index = 0
  private val name: String = "BusinessName"

  private val otherAsset = OtherAssetType(
    description = "Other Asset",
    value = 4000
  )

  def userAnswers = UserAnswers("internalId", "identifier", LocalDate.now, isMigratingToTaxable = true)
    .set(IndexPage, index).success.value
    .set(OtherAssetDescriptionPage, "Other Asset").success.value
    .set(OtherAssetValuePage, 4000L).success.value



  "PropertyOrLand Answers Controller" must {

    "return OK and the correct view for a GET for a given index" in {

      val mockService : TrustService = mock[TrustService]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          bind[TrustService].toInstance(mockService)
        )
        .build()

      when(mockService.getOtherAsset(any(), any())(any(), any()))
        .thenReturn(Future.successful(otherAsset))

      val request = FakeRequest(GET, answersRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[AnswersView]
      val printHelper = application.injector.instanceOf[OtherPrintHelper]
      val answerSection = printHelper(userAnswers, provisional = false, name)

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

      when(mockTrustConnector.amendOtherAsset(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, submitAnswersRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

  }
}
