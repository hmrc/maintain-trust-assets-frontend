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

package navigation

import java.time.{LocalDate, ZoneOffset}

import base.SpecBase
import generators.Generators
import models.{NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.partnership._
import controllers.asset.partnership.routes

trait PartnershipRoutes {

  self: ScalaCheckPropertyChecks with Generators with SpecBase =>

  private val index = 0
  val validDate: LocalDate = LocalDate.now(ZoneOffset.UTC)

  private val navigator: Navigator = injector.instanceOf[PartnershipNavigator]

  def partnershipRoutes(): Unit = {

    "navigate from PartnershipDescriptionPage to PartnershipStartDatePage" in {

      val page = PartnershipDescriptionPage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val answers = userAnswers.set(page, "Partnership Description").success.value
          navigator.nextPage(page, NormalMode, fakeDraftId)(answers)
            .mustBe(routes.PartnershipStartDateController.onPageLoad(NormalMode, index, fakeDraftId))
      }
    }

    "navigate from PartnershipStartDatePage to PartnershipAnswersPage" in {

      val page = PartnershipDescriptionPage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val answers = userAnswers
            .set(page, "Partnership Description").success.value
            .set(PartnershipStartDatePage(index), validDate).success.value
          navigator.nextPage(page, NormalMode, fakeDraftId)(answers)
            .mustBe(routes.PartnershipAnswerController.onPageLoad(index, fakeDraftId))
      }
    }

    "navigate from PartnershipAnswerPage" in {

      val page = PartnershipAnswerPage

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, NormalMode, fakeDraftId)(userAnswers)
            .mustBe(controllers.asset.routes.AddAssetsController.onPageLoad(fakeDraftId))
      }
    }

  }

}
