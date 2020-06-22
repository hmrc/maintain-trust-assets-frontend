/*
 * Copyright 2020 HM Revenue & Customs
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
import controllers.actions.{DraftIdRetrievalActionProvider, FakeDraftIdRetrievalActionProvider, FakeIdentifyForRegistration, RegistrationDataRequiredAction, RegistrationDataRequiredActionImpl, RegistrationIdentifierAction}
import models.{Status, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.scalatest.TryValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice._
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Injector, bind}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import repositories.RegistrationsRepository
import uk.gov.hmrc.auth.core.{AffinityGroup, Enrolment, Enrolments}
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation

trait SpecBase extends PlaySpec
  with GuiceOneAppPerSuite
  with TryValues
  with ScalaFutures
  with IntegrationPatience
  with Mocked
  with FakeTrustsApp {

  lazy val draftId = "id"
  lazy val userInternalId = "internalId"

  def emptyUserAnswers = UserAnswers(draftId, Json.obj(), internalAuthId = userInternalId)

  lazy val fakeNavigator = new FakeNavigator(frontendAppConfig)

  private def fakeDraftIdAction(userAnswers: Option[UserAnswers]) = new FakeDraftIdRetrievalActionProvider(
    "draftId",
    Status.InProgress,
    userAnswers,
    registrationsRepository
  )

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None,
                                   affinityGroup: AffinityGroup = AffinityGroup.Organisation,
                                   enrolments: Enrolments = Enrolments(Set.empty[Enrolment]),
                                   navigator: Navigator = fakeNavigator
                                  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[RegistrationDataRequiredAction].to[RegistrationDataRequiredActionImpl],
        bind[RegistrationIdentifierAction].toInstance(
          new FakeIdentifyForRegistration(affinityGroup, frontendAppConfig)(injectedParsers, trustsAuth, enrolments)
        ),
        bind[DraftIdRetrievalActionProvider].toInstance(fakeDraftIdAction(userAnswers)),
        bind[RegistrationsRepository].toInstance(registrationsRepository),
        bind[AffinityGroup].toInstance(Organisation)
      )
}
