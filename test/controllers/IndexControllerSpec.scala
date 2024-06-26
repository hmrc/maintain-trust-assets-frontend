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

package controllers

import base.SpecBase
import connectors.TrustsConnector
import models.TaskStatus.InProgress
import models.http.TaxableMigrationFlag
import models.{TrustDetails, TypeOfTrust, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustsStoreService
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate
import scala.concurrent.Future

class IndexControllerSpec extends SpecBase with BeforeAndAfterEach {

  val mockTrustsConnector: TrustsConnector = mock[TrustsConnector]
  val mockTrustsStoreService: TrustsStoreService = mock[TrustsStoreService]

  val identifier = "1234567890"
  val startDate: LocalDate = LocalDate.parse("2019-06-01")
  val trustType: TypeOfTrust = TypeOfTrust.WillTrustOrIntestacyTrust
  val is5mldEnabled = false
  val isTaxable = false
  val isUnderlyingData5mld = false

  override def beforeEach(): Unit = {
    reset(mockTrustsConnector)
    reset(mockTrustsStoreService)
    reset(playbackRepository)

    when(playbackRepository.set(any()))
      .thenReturn(Future.successful(true))

    when(mockTrustsStoreService.is5mldEnabled()(any(), any()))
      .thenReturn(Future.successful(is5mldEnabled))

    when(mockTrustsConnector.isTrust5mld(any())(any(), any()))
      .thenReturn(Future.successful(isUnderlyingData5mld))

    when(mockTrustsStoreService.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, "")))
  }

  "Index Controller" must {

    "populate user answers and redirect" in {

      when(mockTrustsConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(startDate = startDate, typeOfTrust = Some(trustType), trustTaxable = Some(isTaxable))))

      when(mockTrustsConnector.getTrustMigrationFlag(any())(any(), any()))
        .thenReturn(Future.successful(TaxableMigrationFlag(Some(false))))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[TrustsConnector].toInstance(mockTrustsConnector),
          bind[TrustsStoreService].toInstance(mockTrustsStoreService)
        ).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(identifier).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad().url)

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)

      uaCaptor.getValue.internalId mustBe "id"
      uaCaptor.getValue.identifier mustBe identifier
      uaCaptor.getValue.whenTrustSetup mustBe startDate
      uaCaptor.getValue.is5mldEnabled mustBe is5mldEnabled
      uaCaptor.getValue.isTaxable mustBe isTaxable
      uaCaptor.getValue.isUnderlyingData5mld mustBe isUnderlyingData5mld

      verify(mockTrustsStoreService).updateTaskStatus(eqTo(identifier), eqTo(InProgress))(any(), any())

      application.stop()
    }

    "default isTaxable to true if trustTaxable is None i.e. 4mld" in {

      when(mockTrustsConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(startDate = startDate, typeOfTrust = Some(trustType), trustTaxable = None)))

      when(mockTrustsConnector.getTrustMigrationFlag(any())(any(), any()))
        .thenReturn(Future.successful(TaxableMigrationFlag(Some(false))))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[TrustsConnector].toInstance(mockTrustsConnector),
          bind[TrustsStoreService].toInstance(mockTrustsStoreService)
        ).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(identifier).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad().url)

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)

      uaCaptor.getValue.isTaxable mustBe true

      verify(mockTrustsStoreService).updateTaskStatus(eqTo(identifier), eqTo(InProgress))(any(), any())

      application.stop()
    }

    "migrating from non-taxable to taxable" in {

      when(mockTrustsConnector.getTrustDetails(any())(any(), any()))
        .thenReturn(Future.successful(TrustDetails(startDate = startDate, typeOfTrust = Some(trustType), trustTaxable = None)))

      when(mockTrustsConnector.getTrustMigrationFlag(any())(any(), any()))
        .thenReturn(Future.successful(TaxableMigrationFlag(Some(true))))

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[TrustsConnector].toInstance(mockTrustsConnector),
          bind[TrustsStoreService].toInstance(mockTrustsStoreService)
        ).build()

      val request = FakeRequest(GET, routes.IndexController.onPageLoad(identifier).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url)

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(playbackRepository).set(uaCaptor.capture)

      uaCaptor.getValue.isMigratingToTaxable mustBe true

      verify(mockTrustsStoreService).updateTaskStatus(eqTo(identifier), eqTo(InProgress))(any(), any())

      application.stop()
    }
  }
}
