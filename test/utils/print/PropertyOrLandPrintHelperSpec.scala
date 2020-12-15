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

package utils.print

import base.SpecBase
import controllers.asset.property_or_land.routes._
import controllers.asset.routes.WhatKindOfAssetController
import models.WhatKindOfAsset.PropertyOrLand
import models.{InternationalAddress, UKAddress, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class PropertyOrLandPrintHelperSpec extends SpecBase {

  private val helper: PropertyOrLandPrintHelper = injector.instanceOf[PropertyOrLandPrintHelper]

  private val index: Int = 0

  private val heading: String = s"Property or land ${index + 1}"

  private val description: String = "Description"
  private val ukAddress: UKAddress = UKAddress("Line 1", "Line 2", None, None, "AB1 1AB")
  private val nonUkAddress: InternationalAddress = InternationalAddress("Line 1", "Line 2", None, "FR")
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
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Property or land"), Some(WhatKindOfAssetController.onPageLoad(index, fakeDraftId).url)),
    AnswerRow("propertyOrLand.addressYesNo.checkYourAnswersLabel", Html("Yes"), Some(PropertyOrLandAddressYesNoController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.addressUkYesNo.checkYourAnswersLabel", Html("Yes"), Some(PropertyOrLandAddressUkYesNoController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.ukAddress.checkYourAnswersLabel", Html("Line 1<br />Line 2<br />AB1 1AB"), Some(PropertyOrLandUKAddressController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.totalValue.checkYourAnswersLabel", Html("£100"), Some(PropertyOrLandTotalValueController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel", Html("Yes"), Some(TrustOwnAllThePropertyOrLandController.onPageLoad(index, draftId).url))
  )

  private val nonUkAddressTrustOwnsAllRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Property or land"), Some(WhatKindOfAssetController.onPageLoad(index, fakeDraftId).url)),
    AnswerRow("propertyOrLand.addressYesNo.checkYourAnswersLabel", Html("Yes"), Some(PropertyOrLandAddressYesNoController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.addressUkYesNo.checkYourAnswersLabel", Html("Yes"), Some(PropertyOrLandAddressUkYesNoController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.internationalAddress.checkYourAnswersLabel", Html("Line 1<br />Line 2<br />France"), Some(PropertyOrLandInternationalAddressController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.totalValue.checkYourAnswersLabel", Html("£100"), Some(PropertyOrLandTotalValueController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel", Html("Yes"), Some(TrustOwnAllThePropertyOrLandController.onPageLoad(index, draftId).url))
  )

  private val descriptionTrustDoesNotOwnAllRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Property or land"), Some(WhatKindOfAssetController.onPageLoad(index, fakeDraftId).url)),
    AnswerRow("propertyOrLand.addressYesNo.checkYourAnswersLabel", Html("No"), Some(PropertyOrLandAddressYesNoController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.description.checkYourAnswersLabel", Html(description), Some(PropertyOrLandDescriptionController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.totalValue.checkYourAnswersLabel", Html("£100"), Some(PropertyOrLandTotalValueController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.trustOwnAllYesNo.checkYourAnswersLabel", Html("No"), Some(TrustOwnAllThePropertyOrLandController.onPageLoad(index, draftId).url)),
    AnswerRow("propertyOrLand.valueInTrust.checkYourAnswersLabel", Html("£50"), Some(PropertyLandValueTrustController.onPageLoad(index, draftId).url))
  )

  "PropertyOrLandPrintHelper" when {

    "printSection" must {
      "render answer section with heading" when {

        "property/land has a UK address and the trust owns all the property/land" in {

          val result: AnswerSection = helper.printSection(
            userAnswers = ukAddressTrustOwnsAllAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe AnswerSection(
            headingKey = Some(heading),
            rows = ukAddressTrustOwnsAllRows
          )
        }

        "property/land has a non-UK address and the trust owns all the property/land" in {

          val result: AnswerSection = helper.printSection(
            userAnswers = nonUkAddressTrustOwnsAllAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe AnswerSection(
            headingKey = Some(heading),
            rows = nonUkAddressTrustOwnsAllRows
          )
        }

        "property/land has a description and the trust doesn't own all the property/land" in {

          val result: AnswerSection = helper.printSection(
            userAnswers = descriptionTrustDoesNotOwnAllAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe AnswerSection(
            headingKey = Some(heading),
            rows = descriptionTrustDoesNotOwnAllRows
          )
        }
      }
    }

    "checkDetailsSection" must {
      "render answer section without heading" when {

        "property/land has a UK address and the trust owns all the property/land" in {

          val result: Seq[AnswerSection] = helper.checkDetailsSection(
            userAnswers = ukAddressTrustOwnsAllAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe Seq(AnswerSection(
            headingKey = None,
            rows = ukAddressTrustOwnsAllRows
          ))
        }

        "property/land has a non-UK address and the trust owns all the property/land" in {

          val result: Seq[AnswerSection] = helper.checkDetailsSection(
            userAnswers = nonUkAddressTrustOwnsAllAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe Seq(AnswerSection(
            headingKey = None,
            rows = nonUkAddressTrustOwnsAllRows
          ))
        }

        "property/land has a description and the trust doesn't own all the property/land" in {

          val result: Seq[AnswerSection] = helper.checkDetailsSection(
            userAnswers = descriptionTrustDoesNotOwnAllAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe Seq(AnswerSection(
            headingKey = None,
            rows = descriptionTrustDoesNotOwnAllRows
          ))
        }
      }
    }
  }
}
