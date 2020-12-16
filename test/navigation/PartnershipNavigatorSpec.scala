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

import base.SpecBase
import controllers.asset.partnership.routes._
import generators.Generators
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.partnership._

import java.time.{LocalDate, ZoneOffset}

class PartnershipNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[PartnershipNavigator]
  private val index: Int = 0
  private val validDate: LocalDate = LocalDate.now(ZoneOffset.UTC)

  "Partnership Navigator" must {

    "navigate from PartnershipDescriptionPage to PartnershipStartDatePage" in {

      val page = PartnershipDescriptionPage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val answers = userAnswers.set(page, "Partnership Description").success.value
          navigator.nextPage(page, fakeDraftId)(answers)
            .mustBe(PartnershipStartDateController.onPageLoad(index, fakeDraftId))
      }
    }

    "navigate from PartnershipStartDatePage to PartnershipAnswersPage" in {

      val page = PartnershipStartDatePage(index)

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          val answers = userAnswers
            .set(PartnershipStartDatePage(index), validDate).success.value
          navigator.nextPage(page, fakeDraftId)(answers)
            .mustBe(PartnershipAnswerController.onPageLoad(index, fakeDraftId))
      }
    }

    "navigate from PartnershipAnswerPage to AddAssetsPage" in {

      val page = PartnershipAnswerPage

      forAll(arbitrary[UserAnswers]) {
        userAnswers =>
          navigator.nextPage(page, fakeDraftId)(userAnswers)
            .mustBe(controllers.asset.routes.AddAssetsController.onPageLoad(fakeDraftId))
      }
    }
  }

}
