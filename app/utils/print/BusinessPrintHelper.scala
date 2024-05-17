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

import controllers.asset.business.routes._
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.asset.business._
import play.api.i18n.Messages
import utils.AnswerRowConverter
import viewmodels.{AnswerRow, AnswerSection}
import javax.inject.Inject

class BusinessPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, name: String)(implicit messages: Messages): AnswerSection = {

    val bound: answerRowConverter.Bound = answerRowConverter.bind(userAnswers, name)

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (provisional) NormalMode else CheckMode
      Seq(
        bound.assetTypeQuestion(0),
        bound.stringQuestion(BusinessNamePage, "business.name", BusinessNameController.onPageLoad(mode).url),
        bound.stringQuestion(BusinessDescriptionPage, "business.description", BusinessDescriptionController.onPageLoad(mode).url),
        bound.yesNoQuestion(BusinessAddressUkYesNoPage, "business.addressUkYesNo", BusinessAddressUkYesNoController.onPageLoad(mode).url),
        bound.addressQuestion(BusinessUkAddressPage, "business.ukAddress", BusinessUkAddressController.onPageLoad(mode).url),
        bound.addressQuestion(BusinessInternationalAddressPage, "business.internationalAddress", BusinessInternationalAddressController.onPageLoad(mode).url),
        bound.currencyQuestion(BusinessValuePage, "business.currentValue", BusinessValueController.onPageLoad(mode).url)
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)

  }

}
