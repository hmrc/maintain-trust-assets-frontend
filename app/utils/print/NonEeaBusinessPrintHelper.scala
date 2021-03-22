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
import models.UserAnswers
import pages.asset.noneeabusiness._
import play.api.i18n.Messages
import utils.{AnswerRowConverter, CheckAnswersFormatters}
import viewmodels.AnswerRow

import javax.inject.Inject

class NonEeaBusinessPrintHelper @Inject()(checkAnswersFormatters: CheckAnswersFormatters) extends PrintHelper {

  override val assetType: String = "nonEeaBusinessAsset"

  override def answerRows(userAnswers: UserAnswers,
                          arg: String,
                          index: Int)
                         (implicit messages: Messages): Seq[AnswerRow] = {

    val converter: AnswerRowConverter = new AnswerRowConverter(checkAnswersFormatters)(userAnswers, arg)

    Seq(
      if (userAnswers.isTaxable) converter.assetTypeQuestion(index) else None,
      converter.stringQuestion(NamePage(index), "nonEeaBusiness.name", NameController.onPageLoad(index).url),
      converter.addressQuestion(InternationalAddressPage(index), "nonEeaBusiness.internationalAddress", InternationalAddressController.onPageLoad(index).url),
      converter.countryQuestion(GoverningCountryPage(index), "nonEeaBusiness.governingCountry", GoverningCountryController.onPageLoad(index).url),
      converter.dateQuestion(StartDatePage(index), "nonEeaBusiness.startDate", StartDateController.onPageLoad(index).url)
    ).flatten
  }
}
