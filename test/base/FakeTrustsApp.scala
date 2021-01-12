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

package base

import config.FrontendAppConfig
import controllers.actions.TrustsAuthorisedFunctions
import org.scalatest.TestSuite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.{AnyContentAsEmpty, BodyParsers, Request}
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext

trait FakeTrustsApp extends GuiceOneAppPerSuite {
  this: TestSuite =>

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = {
    val config: Configuration = injector.instanceOf[FrontendAppConfig].configuration
    new FrontendAppConfig(config) {
      override def accessibilityLinkUrl(implicit request: Request[_]): String =
        "http://localhost:9781/trusts-registration/accessibility?userAction=[]"
    }
  }

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  def injectedParsers: BodyParsers.Default = injector.instanceOf[BodyParsers.Default]

  def trustsAuth: TrustsAuthorisedFunctions = injector.instanceOf[TrustsAuthorisedFunctions]

  implicit def executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)

}