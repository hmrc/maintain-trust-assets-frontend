/*
 * Copyright 2024 HM Revenue & Customs
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
import models.{NonUkAddress, NormalMode, UkAddress, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class PropertyOrLandPrintHelperSpec extends SpecBase {

  private val helper: PropertyOrLandPrintHelper = injector.instanceOf[PropertyOrLandPrintHelper]

  private val description: String = "Description"
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB")
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "FR")
  private val amount: Long = 100L
  private val secondaryAmount: Long = 50L

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
    .set(PropertyOrLandTotalValuePage(index), amount).success.value

  private val addressTrustOwnsAllAnswers: UserAnswers = baseAnswers
    .set(PropertyOrLandAddressYesNoPage(index), true).success.value
    .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value

  private val ukAddressTrustOwnsAllAnswers: UserAnswers = addressTrustOwnsAllAnswers
    .set(PropertyOrLandAddressUkYesNoPage(index), true).success.value
    .set(PropertyOrLandUKAddressPage(index), ukAddress).success.value

  private val nonUkAddressTrustOwnsAllAnswers: UserAnswers = addressTrustOwnsAllAnswers
    .set(PropertyOrLandAddressUkYesNoPage(index), true).success.value
    .set(PropertyOrLandInternationalAddressPage(index), nonUkAddress).success.value

  private val descriptionTrustDoesNotOwnAllAnswers: UserAnswers = baseAnswers
    .set(PropertyOrLandAddressYesNoPage(index), false).success.value
    .set(PropertyOrLandDescriptionPage(index), description).success.value
    .set(TrustOwnAllThePropertyOrLandPage(index), false).success.value
    .set(PropertyLandValueTrustPage(index), secondaryAmount).success.value

  private val ukAddressTrustOwnsAllRows: Seq[AnswerRow] = Seq(
    AnswerRow(label = messages("whatKindOfAsset.checkYourAnswersLabel"), Html("Property or land"), WhatKindOfAssetController.onPageLoad(index).url),
    AnswerRow(label = messages("propertyOrLand.addressYesNo.checkYourAnswersLabel"), Html("Yes"), PropertyOrLandAddressYesNoController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.addressUkYesNo.checkYourAnswersLabel"), Html("Yes"), PropertyOrLandAddressUkYesNoController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.ukAddress.checkYourAnswersLabel"), Html("Line 1<br />Line 2<br />AB1 1AB"), PropertyOrLandUKAddressController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.totalValue.checkYourAnswersLabel"), Html("£100"), PropertyOrLandTotalValueController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel"), Html("Yes"), TrustOwnAllThePropertyOrLandController.onPageLoad(index, NormalMode).url)
  )

  private val nonUkAddressTrustOwnsAllRows: Seq[AnswerRow] = Seq(
    AnswerRow(label = messages("whatKindOfAsset.checkYourAnswersLabel"), Html("Property or land"), WhatKindOfAssetController.onPageLoad(index).url),
    AnswerRow(label = messages("propertyOrLand.addressYesNo.checkYourAnswersLabel"), Html("Yes"), PropertyOrLandAddressYesNoController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.addressUkYesNo.checkYourAnswersLabel"), Html("Yes"), PropertyOrLandAddressUkYesNoController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.internationalAddress.checkYourAnswersLabel"), Html("Line 1<br />Line 2<br />France"), PropertyOrLandInternationalAddressController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.totalValue.checkYourAnswersLabel"), Html("£100"), PropertyOrLandTotalValueController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel"), Html("Yes"), TrustOwnAllThePropertyOrLandController.onPageLoad(index, NormalMode).url)
  )

  private val descriptionTrustDoesNotOwnAllRows: Seq[AnswerRow] = Seq(
    AnswerRow(label = messages("whatKindOfAsset.checkYourAnswersLabel"), Html("Property or land"), WhatKindOfAssetController.onPageLoad(index).url),
    AnswerRow(label = messages("propertyOrLand.addressYesNo.checkYourAnswersLabel"), Html("No"), PropertyOrLandAddressYesNoController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.description.checkYourAnswersLabel"), Html(description), PropertyOrLandDescriptionController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.totalValue.checkYourAnswersLabel"), Html("£100"), PropertyOrLandTotalValueController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel"), Html("No"), TrustOwnAllThePropertyOrLandController.onPageLoad(index, NormalMode).url),
    AnswerRow(label = messages("propertyOrLand.valueInTrust.checkYourAnswersLabel"), Html("£50"), PropertyLandValueTrustController.onPageLoad(index, NormalMode).url)
  )

  "PropertyOrLandPrintHelper" when {

    "generate Property or Land Asset section" when {

      "added" when {

        "property/land has a UK address and the trust owns all the property/land" in {

          val result = helper(ukAddressTrustOwnsAllAnswers, index, provisional = true, description)

          result mustBe AnswerSection(
            headingKey = None,
            rows = ukAddressTrustOwnsAllRows
          )
        }

        "property/land has a non-UK address and the trust owns all the property/land" in {

          val result = helper(nonUkAddressTrustOwnsAllAnswers, index, provisional = true, description)

          result mustBe AnswerSection(
            headingKey = None,
            rows = nonUkAddressTrustOwnsAllRows
          )
        }

        "property/land has a description and the trust doesn't own all the property/land" in {

          val result = helper(descriptionTrustDoesNotOwnAllAnswers, index, provisional = true, description)

          result mustBe AnswerSection(
            headingKey = None,
            rows = descriptionTrustDoesNotOwnAllRows
          )
        }
      }

    }

  }
}
