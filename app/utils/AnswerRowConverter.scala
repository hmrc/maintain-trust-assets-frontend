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

package utils

import com.google.inject.Inject
import controllers.asset.routes.WhatKindOfAssetController
import models.{Address, ShareClass, UserAnswers}
import pages.asset.WhatKindOfAssetPage
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.twirl.api.HtmlFormat
import queries.Gettable
import utils.countryOptions.CountryOptions
import viewmodels.AnswerRow

import java.time.LocalDate

class AnswerRowConverter @Inject()(countryOptions: CountryOptions,
                                   checkAnswersFormatters: CheckAnswersFormatters)
                                  (userAnswers: UserAnswers,
                                   arg: String)
                                  (implicit messages: Messages) {

  def stringQuestion(query: Gettable[String],
                     labelKey: String,
                     changeUrl: String): Option[AnswerRow] = {

    userAnswers.get(query).map(x =>
      AnswerRow(
        label = s"$labelKey.checkYourAnswersLabel",
        answer = HtmlFormat.escape(x),
        changeUrl = Some(changeUrl),
        labelArg = arg
      )
    )
  }

  def yesNoQuestion(query: Gettable[Boolean],
                    labelKey: String,
                    changeUrl: String): Option[AnswerRow] = {

    userAnswers.get(query).map(x =>
      AnswerRow(
        label = s"$labelKey.checkYourAnswersLabel",
        answer = checkAnswersFormatters.yesOrNo(x),
        changeUrl = Some(changeUrl),
        labelArg = arg
      )
    )
  }

  def addressQuestion[T <: Address](query: Gettable[T],
                                    labelKey: String,
                                    changeUrl: String)
                                   (implicit reads: Reads[T]): Option[AnswerRow] = {

    userAnswers.get(query).map(x =>
      AnswerRow(
        label = s"$labelKey.checkYourAnswersLabel",
        answer = checkAnswersFormatters.addressFormatter(x, countryOptions),
        changeUrl = Some(changeUrl),
        labelArg = arg
      )
    )
  }

  def currencyQuestion(query: Gettable[Long],
                       labelKey: String,
                       changeUrl: String): Option[AnswerRow] = {

    userAnswers.get(query).map(x =>
      AnswerRow(
        label = s"$labelKey.checkYourAnswersLabel",
        answer = checkAnswersFormatters.currency(x.toString),
        changeUrl = Some(changeUrl),
        labelArg = arg
      )
    )
  }

  def dateQuestion(query: Gettable[LocalDate],
                   labelKey: String,
                   changeUrl: String): Option[AnswerRow] = {

    userAnswers.get(query).map(x =>
      AnswerRow(
        label = s"$labelKey.checkYourAnswersLabel",
        answer = checkAnswersFormatters.formatDate(x),
        changeUrl = Some(changeUrl),
        labelArg = arg
      )
    )
  }

  def assetTypeQuestion(index: Int,
                        draftId: String): Option[AnswerRow] = {

    val label: String = if (index == 0) "first" else "next"

    userAnswers.get(WhatKindOfAssetPage(index)).map(x =>
      AnswerRow(
        label = s"whatKindOfAsset.$label.checkYourAnswersLabel",
        answer = HtmlFormat.escape(messages(s"whatKindOfAsset.$x")),
        changeUrl = Some(WhatKindOfAssetController.onPageLoad(index, draftId).url),
      )
    )
  }

  def shareClassQuestion(query: Gettable[ShareClass],
                         labelKey: String,
                         changeUrl: String): Option[AnswerRow] = {

    userAnswers.get(query).map(x =>
      AnswerRow(
        label = s"$labelKey.checkYourAnswersLabel",
        answer = HtmlFormat.escape(messages(s"shares.class.$x")),
        changeUrl = Some(changeUrl),
        labelArg = arg
      )
    )
  }
}
