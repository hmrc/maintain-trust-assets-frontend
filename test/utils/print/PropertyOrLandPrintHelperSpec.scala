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

package utils.print

import base.SpecBase
import controllers.asset.property_or_land.routes._
import controllers.asset.routes.WhatKindOfAssetController
import models.WhatKindOfAsset.PropertyOrLand
import models.{InternationalAddress, NormalMode, UKAddress, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class PropertyOrLandPrintHelperSpec extends SpecBase {

  private val helper: PropertyOrLandPrintHelper = injector.instanceOf[PropertyOrLandPrintHelper]

  private val description: String = "Description"
  private val ukAddress: UKAddress = UKAddress("Line 1", "Line 2", None, None, "AB1 1AB")
  private val nonUkAddress: InternationalAddress = InternationalAddress("Line 1", "Line 2", None, "FR")
  private val amount: Long = 100L
  private val secondaryAmount: Long = 50L

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage, PropertyOrLand).success.value
    .set(PropertyOrLandTotalValuePage, amount).success.value

  private val addressTrustOwnsAllAnswers: UserAnswers = baseAnswers
    .set(PropertyOrLandAddressYesNoPage, true).success.value
    .set(TrustOwnAllThePropertyOrLandPage, true).success.value

  private val ukAddressTrustOwnsAllAnswers: UserAnswers = addressTrustOwnsAllAnswers
    .set(PropertyOrLandAddressUkYesNoPage, true).success.value
    .set(PropertyOrLandUKAddressPage, ukAddress).success.value

  private val nonUkAddressTrustOwnsAllAnswers: UserAnswers = addressTrustOwnsAllAnswers
    .set(PropertyOrLandAddressUkYesNoPage, true).success.value
    .set(PropertyOrLandInternationalAddressPage, nonUkAddress).success.value

  private val descriptionTrustDoesNotOwnAllAnswers: UserAnswers = baseAnswers
    .set(PropertyOrLandAddressYesNoPage, false).success.value
    .set(PropertyOrLandDescriptionPage, description).success.value
    .set(TrustOwnAllThePropertyOrLandPage, false).success.value
    .set(PropertyLandValueTrustPage, secondaryAmount).success.value

  private val ukAddressTrustOwnsAllRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Property or land"), Some(WhatKindOfAssetController.onPageLoad().url)),
    AnswerRow("propertyOrLand.addressYesNo.checkYourAnswersLabel", Html("Yes"), Some(PropertyOrLandAddressYesNoController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.addressUkYesNo.checkYourAnswersLabel", Html("Yes"), Some(PropertyOrLandAddressUkYesNoController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.ukAddress.checkYourAnswersLabel", Html("Line 1<br />Line 2<br />AB1 1AB"), Some(PropertyOrLandUKAddressController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.totalValue.checkYourAnswersLabel", Html("£100"), Some(PropertyOrLandTotalValueController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel", Html("Yes"), Some(TrustOwnAllThePropertyOrLandController.onPageLoad(NormalMode).url))
  )

  private val nonUkAddressTrustOwnsAllRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Property or land"), Some(WhatKindOfAssetController.onPageLoad().url)),
    AnswerRow("propertyOrLand.addressYesNo.checkYourAnswersLabel", Html("Yes"), Some(PropertyOrLandAddressYesNoController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.addressUkYesNo.checkYourAnswersLabel", Html("Yes"), Some(PropertyOrLandAddressUkYesNoController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.internationalAddress.checkYourAnswersLabel", Html("Line 1<br />Line 2<br />France"), Some(PropertyOrLandInternationalAddressController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.totalValue.checkYourAnswersLabel", Html("£100"), Some(PropertyOrLandTotalValueController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel", Html("Yes"), Some(TrustOwnAllThePropertyOrLandController.onPageLoad(NormalMode).url))
  )

  private val descriptionTrustDoesNotOwnAllRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Property or land"), Some(WhatKindOfAssetController.onPageLoad().url)),
    AnswerRow("propertyOrLand.addressYesNo.checkYourAnswersLabel", Html("No"), Some(PropertyOrLandAddressYesNoController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.description.checkYourAnswersLabel", Html(description), Some(PropertyOrLandDescriptionController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.totalValue.checkYourAnswersLabel", Html("£100"), Some(PropertyOrLandTotalValueController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel", Html("No"), Some(TrustOwnAllThePropertyOrLandController.onPageLoad(NormalMode).url)),
    AnswerRow("propertyOrLand.valueInTrust.checkYourAnswersLabel", Html("£50"), Some(PropertyLandValueTrustController.onPageLoad(NormalMode).url))
  )

  "PropertyOrLandPrintHelper" when {

    "generate Property or Land Asset section" when {

      "added" when {

        "property/land has a UK address and the trust owns all the property/land" in {

          val result = helper(ukAddressTrustOwnsAllAnswers, provisional = true, description)

          result mustBe AnswerSection(
            headingKey = None,
            rows = ukAddressTrustOwnsAllRows
          )
        }

        "property/land has a non-UK address and the trust owns all the property/land" in {

          val result = helper(nonUkAddressTrustOwnsAllAnswers, provisional = true, description)

          result mustBe AnswerSection(
            headingKey = None,
            rows = nonUkAddressTrustOwnsAllRows
          )
        }

        "property/land has a description and the trust doesn't own all the property/land" in {

          val result = helper(descriptionTrustDoesNotOwnAllAnswers, provisional = true, description)

          result mustBe AnswerSection(
            headingKey = None,
            rows = descriptionTrustDoesNotOwnAllRows
          )
        }
      }

    }

  }
}
