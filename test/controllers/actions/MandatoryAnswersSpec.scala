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

package controllers.actions

import base.SpecBase
import models.WhatKindOfAsset.{Other, PropertyOrLand}
import models.{NormalMode, LinearPageInJourney, NonLinearPageInJourney}
import pages.asset.WhatKindOfAssetPage
import pages.asset.other._
import pages.asset.property_or_land._

class MandatoryAnswersSpec extends SpecBase {

  private val index: Int = 0

  "Mandatory Answers" when {

    "linear journey" when {

      val pages = List(
        LinearPageInJourney(page = WhatKindOfAssetPage(index)),
        LinearPageInJourney(page = OtherAssetDescriptionPage(index)),
        LinearPageInJourney(page = OtherAssetValuePage(index))
      )

      val cya = controllers.asset.other.routes.OtherAssetAnswersController.onPageLoad(index, fakeDraftId)

      "journey complete" must {

        "redirect to CYA" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), Other).success.value
            .set(OtherAssetDescriptionPage(index), "Description").success.value
            .set(OtherAssetValuePage(index), "4000").success.value

          val mandatoryAnswers = new MandatoryAnswers(userAnswers, fakeDraftId)

          val expected  = cya.url

          mandatoryAnswers.redirect(cya, pages).url mustEqual expected
        }
      }

      "journey not complete" must {

        "redirect to what kind of asset page if not answered" in {

          val mandatoryAnswers = new MandatoryAnswers(emptyUserAnswers, fakeDraftId)

          val expected = controllers.asset.routes.WhatKindOfAssetController.onPageLoad(NormalMode, index, fakeDraftId).url

          mandatoryAnswers.redirect(cya, pages).url mustEqual expected
        }

        "redirect to first page if not answered" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), Other).success.value

          val mandatoryAnswers = new MandatoryAnswers(userAnswers, fakeDraftId)

          val expected = controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode, index, fakeDraftId).url

          mandatoryAnswers.redirect(cya, pages).url mustEqual expected
        }

        "redirect to second page if not answered" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), Other).success.value
            .set(OtherAssetDescriptionPage(index), "Description").success.value

          val mandatoryAnswers = new MandatoryAnswers(userAnswers, fakeDraftId)

          val expected = controllers.asset.other.routes.OtherAssetValueController.onPageLoad(NormalMode, index, fakeDraftId).url

          mandatoryAnswers.redirect(cya, pages).url mustEqual expected
        }
      }
    }

    "non-linear journey" when {

      val pages = List(
        LinearPageInJourney(page = WhatKindOfAssetPage(index)),
        NonLinearPageInJourney(
          page = PropertyOrLandAddressYesNoPage(index),
          yesPages = List(
            NonLinearPageInJourney(
              page = PropertyOrLandAddressUkYesNoPage(index),
              yesPages = List(LinearPageInJourney(PropertyOrLandUKAddressPage(index))),
              noPages = List(LinearPageInJourney(PropertyOrLandInternationalAddressPage(index)))
            )
          ),
          noPages = List(LinearPageInJourney(PropertyOrLandDescriptionPage(index)))
        ),
        LinearPageInJourney(page = PropertyOrLandTotalValuePage(index)),
        NonLinearPageInJourney(
          page = TrustOwnAllThePropertyOrLandPage(index),
          yesPages = Nil,
          noPages = List(LinearPageInJourney(PropertyLandValueTrustPage(index)))
        )
      )

      val cya = controllers.asset.property_or_land.routes.PropertyOrLandAnswerController.onPageLoad(index, fakeDraftId)

      "journey complete" must {

        "redirect to CYA" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), false).success.value
            .set(PropertyOrLandDescriptionPage(index), "Description").success.value
            .set(PropertyOrLandTotalValuePage(index), "4000").success.value
            .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value

          val mandatoryAnswers = new MandatoryAnswers(userAnswers, fakeDraftId)

          val expected  = cya.url

          mandatoryAnswers.redirect(cya, pages).url mustEqual expected
        }
      }

      "journey not complete" must {

        "redirect to what kind of asset page if not answered" in {

          val mandatoryAnswers = new MandatoryAnswers(emptyUserAnswers, fakeDraftId)

          val expected = controllers.asset.routes.WhatKindOfAssetController.onPageLoad(NormalMode, index, fakeDraftId).url

          mandatoryAnswers.redirect(cya, pages).url mustEqual expected
        }

        "redirect to YES pages if yes no question answered TRUE" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), true).success.value

          val mandatoryAnswers = new MandatoryAnswers(userAnswers, fakeDraftId)

          val expected = controllers.asset.property_or_land.routes.PropertyOrLandAddressUkYesNoController.onPageLoad(NormalMode, index, fakeDraftId).url

          mandatoryAnswers.redirect(cya, pages).url mustEqual expected
        }

        "redirect to NO pages if yes no question answered FALSE" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), false).success.value

          val mandatoryAnswers = new MandatoryAnswers(userAnswers, fakeDraftId)

          val expected = controllers.asset.property_or_land.routes.PropertyOrLandDescriptionController.onPageLoad(NormalMode, index, fakeDraftId).url

          mandatoryAnswers.redirect(cya, pages).url mustEqual expected
        }

        "redirect to nested YES pages if yes no question answered TRUE" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), true).success.value
            .set(PropertyOrLandAddressUkYesNoPage(index), true).success.value

          val mandatoryAnswers = new MandatoryAnswers(userAnswers, fakeDraftId)

          val expected = controllers.asset.property_or_land.routes.PropertyOrLandUKAddressController.onPageLoad(NormalMode, index, fakeDraftId).url

          mandatoryAnswers.redirect(cya, pages).url mustEqual expected
        }

        "redirect to nested NO pages if yes no question answered FALSE" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), true).success.value
            .set(PropertyOrLandAddressUkYesNoPage(index), false).success.value

          val mandatoryAnswers = new MandatoryAnswers(userAnswers, fakeDraftId)

          val expected = controllers.asset.property_or_land.routes.PropertyOrLandInternationalAddressController.onPageLoad(NormalMode, index, fakeDraftId).url

          mandatoryAnswers.redirect(cya, pages).url mustEqual expected
        }
      }
    }
  }

}
