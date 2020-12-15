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
import controllers.asset.business.routes._
import controllers.asset.routes.WhatKindOfAssetController
import models.WhatKindOfAsset.Business
import models.{InternationalAddress, NormalMode, UKAddress, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import pages.asset.business._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class BusinessPrintHelperSpec extends SpecBase {

  private val helper: BusinessPrintHelper = injector.instanceOf[BusinessPrintHelper]

  private val index: Int = 0

  private val heading: String = "Business"

  private val name: String = "Name"
  private val description: String = "Description"
  private val ukAddress: UKAddress = UKAddress("Line 1", "Line 2", None, None, "AB1 1AB")
  private val nonUkAddress: InternationalAddress = InternationalAddress("Line 1", "Line 2", None, "FR")
  private val amount: Long = 100L

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage(index), Business).success.value
    .set(BusinessNamePage(index), name).success.value
    .set(BusinessDescriptionPage(index), description).success.value
    .set(BusinessValuePage(index), amount).success.value

  private val ukAddressAnswers: UserAnswers = baseAnswers
    .set(BusinessAddressUkYesNoPage(index), true).success.value
    .set(BusinessUkAddressPage(index), ukAddress).success.value

  private val nonUkAddressAnswers: UserAnswers = baseAnswers
    .set(BusinessAddressUkYesNoPage(index), false).success.value
    .set(BusinessInternationalAddressPage(index), nonUkAddress).success.value

  private val ukAddressRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Business"), Some(WhatKindOfAssetController.onPageLoad(NormalMode, index, fakeDraftId).url)),
    AnswerRow("business.name.checkYourAnswersLabel", Html(name), Some(BusinessNameController.onPageLoad(NormalMode, index, draftId).url)),
    AnswerRow("business.description.checkYourAnswersLabel", Html(description), Some(BusinessDescriptionController.onPageLoad(NormalMode, index, draftId).url)),
    AnswerRow("business.addressUkYesNo.checkYourAnswersLabel", Html("Yes"), Some(BusinessAddressUkYesNoController.onPageLoad(NormalMode, index, draftId).url)),
    AnswerRow("business.ukAddress.checkYourAnswersLabel", Html("Line 1<br />Line 2<br />AB1 1AB"), Some(BusinessUkAddressController.onPageLoad(NormalMode, index, draftId).url)),
    AnswerRow("business.currentValue.checkYourAnswersLabel", Html("£100"), Some(BusinessValueController.onPageLoad(NormalMode, index, draftId).url))
  )

  private val nonUkAddressRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Business"), Some(WhatKindOfAssetController.onPageLoad(NormalMode, index, fakeDraftId).url)),
    AnswerRow("business.name.checkYourAnswersLabel", Html(name), Some(BusinessNameController.onPageLoad(NormalMode, index, draftId).url)),
    AnswerRow("business.description.checkYourAnswersLabel", Html(description), Some(BusinessDescriptionController.onPageLoad(NormalMode, index, draftId).url)),
    AnswerRow("business.addressUkYesNo.checkYourAnswersLabel", Html("No"), Some(BusinessAddressUkYesNoController.onPageLoad(NormalMode, index, draftId).url)),
    AnswerRow("business.internationalAddress.checkYourAnswersLabel", Html("Line 1<br />Line 2<br />France"), Some(BusinessInternationalAddressController.onPageLoad(NormalMode, index, draftId).url)),
    AnswerRow("business.currentValue.checkYourAnswersLabel", Html("£100"), Some(BusinessValueController.onPageLoad(NormalMode, index, draftId).url))
  )

  "BusinessPrintHelper" when {

    "printSection" must {
      "render answer section with heading" when {

        "business has UK address" in {

          val result: AnswerSection = helper.printSection(
            userAnswers = ukAddressAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe AnswerSection(
            headingKey = Some(heading),
            rows = ukAddressRows
          )
        }

        "business has non-UK address" in {

          val result: AnswerSection = helper.printSection(
            userAnswers = nonUkAddressAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe AnswerSection(
            headingKey = Some(heading),
            rows = nonUkAddressRows
          )
        }
      }
    }

    "checkDetailsSection" must {
      "render answer section without heading" when {

        "business has UK address" in {

          val result: Seq[AnswerSection] = helper.checkDetailsSection(
            userAnswers = ukAddressAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe Seq(AnswerSection(
            headingKey = None,
            rows = ukAddressRows
          ))
        }

        "business has non-UK address" in {

          val result: Seq[AnswerSection] = helper.checkDetailsSection(
            userAnswers = nonUkAddressAnswers,
            index = index,
            draftId = fakeDraftId
          )

          result mustBe Seq(AnswerSection(
            headingKey = None,
            rows = nonUkAddressRows
          ))
        }
      }
    }
  }
}
