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
import controllers.asset.noneeabusiness.routes._
import controllers.asset.routes.WhatKindOfAssetController
import models.WhatKindOfAsset.NonEeaBusiness
import models.{InternationalAddress, UKAddress, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import pages.asset.noneeabusiness._
import play.twirl.api.Html
import viewmodels.{AnswerRow, AnswerSection}

import java.time.LocalDate

class NonEeaBusinessPrintHelperSpec extends SpecBase {

  private val helper: NonEeaBusinessPrintHelper = injector.instanceOf[NonEeaBusinessPrintHelper]

  private val index: Int = 0

  private val heading: String = s"Non-EEA Company ${index + 1}"

  private val name: String = "Name"
  private val ukAddress: UKAddress = UKAddress("Line 1", "Line 2", None, None, "AB1 1AB")
  private val country: String = "FR"
  private val nonUkAddress: InternationalAddress = InternationalAddress("Line 1", "Line 2", None, country)
  private val date: LocalDate = LocalDate.parse("1996-02-03")

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage(index), NonEeaBusiness).success.value
    .set(NamePage(index), name).success.value
    .set(GoverningCountryPage(index), country).success.value
    .set(StartDatePage(index), date).success.value

  private val ukAddressAnswers: UserAnswers = baseAnswers
    .set(AddressUkYesNoPage(index), true).success.value
    .set(UkAddressPage(index), ukAddress).success.value

  private val nonUkAddressAnswers: UserAnswers = baseAnswers
    .set(AddressUkYesNoPage(index), false).success.value
    .set(InternationalAddressPage(index), nonUkAddress).success.value

  private val ukAddressRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Non-EEA Company"), Some(WhatKindOfAssetController.onPageLoad(index, fakeDraftId).url)),
    AnswerRow("nonEeaBusiness.name.checkYourAnswersLabel", Html(name), Some(NameController.onPageLoad(index, draftId).url)),
    AnswerRow("nonEeaBusiness.addressUkYesNo.checkYourAnswersLabel", Html("Yes"), Some(AddressUkYesNoController.onPageLoad(index, draftId).url)),
    AnswerRow("nonEeaBusiness.ukAddress.checkYourAnswersLabel", Html("Line 1<br />Line 2<br />AB1 1AB"), Some(UkAddressController.onPageLoad(index, draftId).url)),
    AnswerRow("nonEeaBusiness.governingCountry.checkYourAnswersLabel", Html("France"), Some(GoverningCountryController.onPageLoad(index, draftId).url)),
    AnswerRow("nonEeaBusiness.startDate.checkYourAnswersLabel", Html("3 February 1996"), Some(StartDateController.onPageLoad(index, draftId).url))
  )

  private val nonUkAddressRows: Seq[AnswerRow] = Seq(
    AnswerRow("whatKindOfAsset.first.checkYourAnswersLabel", Html("Non-EEA Company"), Some(WhatKindOfAssetController.onPageLoad(index, fakeDraftId).url)),
    AnswerRow("nonEeaBusiness.name.checkYourAnswersLabel", Html(name), Some(NameController.onPageLoad(index, draftId).url)),
    AnswerRow("nonEeaBusiness.addressUkYesNo.checkYourAnswersLabel", Html("No"), Some(AddressUkYesNoController.onPageLoad(index, draftId).url)),
    AnswerRow("nonEeaBusiness.internationalAddress.checkYourAnswersLabel", Html("Line 1<br />Line 2<br />France"), Some(InternationalAddressController.onPageLoad(index, draftId).url)),
    AnswerRow("nonEeaBusiness.governingCountry.checkYourAnswersLabel", Html("France"), Some(GoverningCountryController.onPageLoad(index, draftId).url)),
    AnswerRow("nonEeaBusiness.startDate.checkYourAnswersLabel", Html("3 February 1996"), Some(StartDateController.onPageLoad(index, draftId).url))
  )

  "NonEeaBusinessPrintHelper" when {

    "printSection" must {
      "render answer section with heading" when {

        "non-EEA business has UK address" in {

          val result: AnswerSection = helper.printSection(
            userAnswers = ukAddressAnswers,
            index = index,
            specificIndex = index,
            draftId = fakeDraftId
          )

          result mustBe AnswerSection(
            headingKey = Some(heading),
            rows = ukAddressRows
          )
        }

        "non-EEA business has non-UK address" in {

          val result: AnswerSection = helper.printSection(
            userAnswers = nonUkAddressAnswers,
            index = index,
            specificIndex = index,
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
