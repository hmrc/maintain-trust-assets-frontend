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

package controllers.actions

import config.FrontendAppConfig
import models.requests.IdentifierRequest
import play.api.mvc._
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, Enrolments}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeIdentifyForRegistration @Inject()(affinityGroup: AffinityGroup, config: FrontendAppConfig)
                                           (override val parser: BodyParsers.Default,
                                            trustsAuth: TrustsAuthorisedFunctions,
                                            enrolments: Enrolments = Enrolments(Set.empty[Enrolment]))
                                           (override implicit val executionContext: ExecutionContext)
  extends RegistrationIdentifierAction(parser, trustsAuth, config) {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] =
    block(IdentifierRequest(request, "id", affinityGroup, enrolments))

  override def composeAction[A](action: Action[A]): Action[A] = new FakeAffinityGroupIdentifierAction(action, trustsAuth, config)

}

class FakeAffinityGroupIdentifierAction[A](action: Action[A], trustsAuth: TrustsAuthorisedFunctions, config: FrontendAppConfig) extends AffinityGroupIdentifierAction(action, trustsAuth, config)  {
  override def apply(request: Request[A]): Future[Result] = {
    action(request)
  }
}