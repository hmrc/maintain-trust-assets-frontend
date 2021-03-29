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

import controllers.asset.noneeabusiness.routes._
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.asset.noneeabusiness._
import play.api.i18n.Messages
import utils.AnswerRowConverter
import viewmodels.{AnswerRow, AnswerSection}
import javax.inject.Inject

class NonEeaBusinessPrintHelper @Inject()(answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, provisional: Boolean, name: String)(implicit messages: Messages): AnswerSection = {

    val bound: answerRowConverter.Bound = answerRowConverter.bind(userAnswers, name)

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (provisional) NormalMode else CheckMode
      Seq(
        if (userAnswers.isTaxable) bound.assetTypeQuestion(0) else None,
        bound.stringQuestion(NamePage, "nonEeaBusiness.name", NameController.onPageLoad(mode).url),
        bound.addressQuestion(InternationalAddressPage, "nonEeaBusiness.internationalAddress", InternationalAddressController.onPageLoad(mode).url),
        bound.countryQuestion(GoverningCountryPage, "nonEeaBusiness.governingCountry", GoverningCountryController.onPageLoad(mode).url),
        bound.dateQuestion(StartDatePage, "nonEeaBusiness.startDate", StartDateController.onPageLoad(mode).url)
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)

  }
}
