/*
 * Copyright 2022 HM Revenue & Customs
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

package repositories

import config.FrontendAppConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

trait MongoSuite extends ScalaFutures {
  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(30, Seconds), interval = Span(500, Millis))

  val application: Application = new GuiceApplicationBuilder()
    .configure(Seq(
      "metrics.enabled" -> false,
      "auditing.enabled" -> false,
      "mongo-async-driver.akka.log-dead-letters" -> 0
    ): _*)

    .build()

  val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

}