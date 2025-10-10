/*
 * Copyright 2025 HM Revenue & Customs
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

  def apply(userAnswers: UserAnswers, index: Int, provisional: Boolean, name: String)(implicit messages: Messages): AnswerSection = {

    val bound: answerRowConverter.Bound = answerRowConverter.bind(userAnswers, name)

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (provisional) NormalMode else CheckMode
      Seq(
        bound.assetTypeQuestion(index),
        bound.stringQuestion(BusinessNamePage(index), "business.name", BusinessNameController.onPageLoad(index, mode).url),
        bound.stringQuestion(BusinessDescriptionPage(index), "business.description", BusinessDescriptionController.onPageLoad(index, mode).url),
        bound.yesNoQuestion(BusinessAddressUkYesNoPage(index), "business.addressUkYesNo", BusinessAddressUkYesNoController.onPageLoad(index, mode).url),
        bound.addressQuestion(BusinessUkAddressPage(index), "business.ukAddress", BusinessUkAddressController.onPageLoad(index, mode).url),
        bound.addressQuestion(BusinessInternationalAddressPage(index), "business.internationalAddress", BusinessInternationalAddressController.onPageLoad(index, mode).url),
        bound.currencyQuestion(BusinessValuePage(index), "business.currentValue", BusinessValueController.onPageLoad(index, mode).url)
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)

  }

}
