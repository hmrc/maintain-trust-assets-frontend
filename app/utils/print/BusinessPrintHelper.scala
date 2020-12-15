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

import controllers.asset.business.routes._
import models.UserAnswers
import pages.asset.business._
import play.api.i18n.Messages
import utils.countryOptions.CountryOptions
import utils.{AnswerRowConverter, CheckAnswersFormatters}
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class BusinessPrintHelper @Inject()(countryOptions: CountryOptions,
                                    checkAnswersFormatters: CheckAnswersFormatters) extends PrintHelper {

  def printSection(userAnswers: UserAnswers,
                   arg: String = "",
                   index: Int,
                   draftId: String)
                  (implicit messages: Messages): AnswerSection = {

    section(userAnswers, arg, index, draftId, Some(messages("answerPage.section.businessAsset.subheading", index + 1)))
  }

  override def answerRows(userAnswers: UserAnswers,
                          arg: String = "",
                          index: Int,
                          draftId: String)
                         (implicit messages: Messages): Seq[AnswerRow] = {

    val converter: AnswerRowConverter = new AnswerRowConverter(countryOptions, checkAnswersFormatters)(userAnswers, arg)

    Seq(
      converter.assetTypeQuestion(index, draftId),
      converter.stringQuestion(BusinessNamePage(index), "business.name", BusinessNameController.onPageLoad(index, draftId).url),
      converter.stringQuestion(BusinessDescriptionPage(index), "business.description", BusinessDescriptionController.onPageLoad(index, draftId).url),
      converter.yesNoQuestion(BusinessAddressUkYesNoPage(index), "business.addressUkYesNo", BusinessAddressUkYesNoController.onPageLoad(index, draftId).url),
      converter.addressQuestion(BusinessUkAddressPage(index), "business.ukAddress", BusinessUkAddressController.onPageLoad(index, draftId).url),
      converter.addressQuestion(BusinessInternationalAddressPage(index), "business.internationalAddress", BusinessInternationalAddressController.onPageLoad(index, draftId).url),
      converter.currencyQuestion(BusinessValuePage(index), "business.currentValue", BusinessValueController.onPageLoad(index, draftId).url)
    ).flatten
  }
}
