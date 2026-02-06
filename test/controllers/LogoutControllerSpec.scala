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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

class LogoutControllerSpec extends SpecBase with MockitoSugar {
  // Mocks
  val mockAppConfig              = mock[FrontendAppConfig]
  val mockAuditConnector         = mock[AuditConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "LogoutController" should {

    "redirect to logoutUrl with feedbackId in session and send audit if auditing is enabled" in {
      when(mockAppConfig.logoutUrl).thenReturn(frontendAppConfig.logoutUrl)
      when(mockAppConfig.logoutAudit).thenReturn(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[AuditConnector].toInstance(mockAuditConnector))
        .overrides(bind[FrontendAppConfig].toInstance(mockAppConfig))
        .build()

      val request = FakeRequest(GET, routes.LogoutController.logout().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result) mustBe Some(frontendAppConfig.logoutUrl)

      verify(mockAuditConnector, times(1))
        .sendExplicitAudit(eqTo("trusts"), org.mockito.ArgumentMatchers.any[Map[String, String]])(any(), any())

      application.stop()

    }

    "redirect to logoutUrl and NOT send audit if auditing is disabled" in {

      reset(mockAppConfig, mockAuditConnector)

      when(mockAppConfig.logoutUrl).thenReturn(frontendAppConfig.logoutUrl)
      when(mockAppConfig.logoutAudit).thenReturn(false)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(
          bind[FrontendAppConfig].toInstance(mockAppConfig),
          bind[AuditConnector].toInstance(mockAuditConnector)
        )
        .build()

      val request = FakeRequest(GET, routes.LogoutController.logout().url)

      val result = route(application, request).value

      status(result)           mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(frontendAppConfig.logoutUrl)

      verify(mockAuditConnector, never())
        .sendExplicitAudit(any(), org.mockito.ArgumentMatchers.any[Map[String, String]])(any(), any())

      application.stop()
    }

  }

}
