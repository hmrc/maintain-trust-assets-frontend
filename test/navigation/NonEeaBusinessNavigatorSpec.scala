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

package navigation

import base.SpecBase
import controllers.asset.noneeabusiness.add.{routes => addRts}
import controllers.asset.noneeabusiness.amend.{routes => amendRts}
import controllers.asset.noneeabusiness.{routes => rts}
import generators.Generators
import models.{CheckMode, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.asset.noneeabusiness._
import pages.asset.noneeabusiness.add.StartDatePage
import pages.asset.noneeabusiness.amend.IndexPage

class NonEeaBusinessNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val navigator: Navigator = injector.instanceOf[NonEeaBusinessNavigator]

  "Non-EEA Business Navigator" when {

    "adding" must {

      val mode = NormalMode

      "navigate from NamePage to InternationalAddressPage" in {

        val page = NamePage(index)

        forAll(arbitrary[UserAnswers]) { userAnswers =>
          navigator
            .nextPage(page, mode, userAnswers)
            .mustBe(rts.InternationalAddressController.onPageLoad(index, mode))
        }
      }

      "navigate from InternationalAddressPage to GoverningCountryPage" in {

        val page = NonUkAddressPage(index)

        forAll(arbitrary[UserAnswers]) { userAnswers =>
          navigator
            .nextPage(page, mode, userAnswers)
            .mustBe(rts.GoverningCountryController.onPageLoad(index, mode))
        }
      }

      "navigate from GoverningCountryPage to StartDatePage" in {

        val page = GoverningCountryPage(index)

        forAll(arbitrary[UserAnswers]) { userAnswers =>
          navigator
            .nextPage(page, mode, userAnswers)
            .mustBe(addRts.StartDateController.onPageLoad(index))
        }
      }

      "navigate from StartDatePage to Check Answers" in {

        val page = StartDatePage(index)

        forAll(arbitrary[UserAnswers]) { userAnswers =>
          navigator
            .nextPage(page, mode, userAnswers)
            .mustBe(addRts.AnswersController.onPageLoad(index))
        }
      }
    }

    "amending" must {

      val mode = CheckMode

      "navigate from NamePage to InternationalAddressPage" in {

        val page = NamePage(index)

        forAll(arbitrary[UserAnswers]) { userAnswers =>
          navigator
            .nextPage(page, mode, userAnswers.set(IndexPage, index).success.value)
            .mustBe(rts.InternationalAddressController.onPageLoad(index, mode))
        }
      }

      "navigate from InternationalAddressPage to GoverningCountryPage" in {

        val page = NonUkAddressPage(index)

        forAll(arbitrary[UserAnswers]) { userAnswers =>
          navigator
            .nextPage(page, mode, userAnswers.set(IndexPage, index).success.value)
            .mustBe(rts.GoverningCountryController.onPageLoad(index, mode))
        }
      }

      "navigate from GoverningCountryPage to Check Answers" in {

        val page = GoverningCountryPage(index)

        forAll(arbitrary[UserAnswers]) { userAnswers =>
          navigator
            .nextPage(page, mode, userAnswers.set(IndexPage, index).success.value)
            .mustBe(amendRts.AnswersController.renderFromUserAnswers(index))
        }
      }
    }
  }

}
