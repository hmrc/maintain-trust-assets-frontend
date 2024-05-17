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
import controllers.asset.business.routes._
import controllers.asset.routes.WhatKindOfAssetController
import models.WhatKindOfAsset.Business
import models.{NonUkAddress, NormalMode, UkAddress, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import pages.asset.business._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

class BusinessPrintHelperSpec extends SpecBase {

  private val helper: BusinessPrintHelper = injector.instanceOf[BusinessPrintHelper]

  private val name: String = "Name"
  private val description: String = "Description"
  private val ukAddress: UkAddress = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB")
  private val nonUkAddress: NonUkAddress = NonUkAddress("Line 1", "Line 2", None, "FR")
  private val amount: Long = 100L

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage, Business).success.value
    .set(BusinessNamePage, name).success.value
    .set(BusinessDescriptionPage, description).success.value
    .set(BusinessValuePage, amount).success.value

  private val ukAddressAnswers: UserAnswers = baseAnswers
    .set(BusinessAddressUkYesNoPage, true).success.value
    .set(BusinessUkAddressPage, ukAddress).success.value

  private val nonUkAddressAnswers: UserAnswers = baseAnswers
    .set(BusinessAddressUkYesNoPage, false).success.value
    .set(BusinessInternationalAddressPage, nonUkAddress).success.value

  private val ukAddressRows: Seq[AnswerRow] = Seq(
    AnswerRow(label = messages("whatKindOfAsset.checkYourAnswersLabel"), Html("Business"), WhatKindOfAssetController.onPageLoad().url),
    AnswerRow(label = messages("business.name.checkYourAnswersLabel"), Html(name), BusinessNameController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("business.description.checkYourAnswersLabel", name), Html(description), BusinessDescriptionController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("business.addressUkYesNo.checkYourAnswersLabel", name), Html("Yes"), BusinessAddressUkYesNoController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("business.ukAddress.checkYourAnswersLabel", name), Html("Line 1<br />Line 2<br />AB1 1AB"), BusinessUkAddressController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("business.currentValue.checkYourAnswersLabel", name), Html("£100"), BusinessValueController.onPageLoad(NormalMode).url)
  )

  private val nonUkAddressRows: Seq[AnswerRow] = Seq(
    AnswerRow(label = messages("whatKindOfAsset.checkYourAnswersLabel"), Html("Business"), WhatKindOfAssetController.onPageLoad().url),
    AnswerRow(label = messages("business.name.checkYourAnswersLabel"), Html(name), BusinessNameController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("business.description.checkYourAnswersLabel", name), Html(description), BusinessDescriptionController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("business.addressUkYesNo.checkYourAnswersLabel", name), Html("No"), BusinessAddressUkYesNoController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("business.internationalAddress.checkYourAnswersLabel", name), Html("Line 1<br />Line 2<br />France"), BusinessInternationalAddressController.onPageLoad(NormalMode).url),
    AnswerRow(label = messages("business.currentValue.checkYourAnswersLabel", name), Html("£100"), BusinessValueController.onPageLoad(NormalMode).url)
  )

  "BusinessPrintHelper" when {

    "generate Business Asset section" when {

      "added" when {

        "business has UK address" in {

          val result = helper(ukAddressAnswers, provisional = true, name)

          result mustBe AnswerSection(
            headingKey = None,
            rows = ukAddressRows
          )
        }

        "business has non-UK address" in {

          val result = helper(nonUkAddressAnswers, provisional = true, name)

          result mustBe AnswerSection(
            headingKey = None,
            rows = nonUkAddressRows
          )
        }

      }

    }

  }
}
