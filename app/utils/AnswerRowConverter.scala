/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{Address, ShareClass, UserAnswers, WhatKindOfAsset}
import pages.asset.WhatKindOfAssetPage
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.twirl.api.{Html, HtmlFormat}
import queries.Gettable
import utils.CheckAnswersFormatters._
import viewmodels.AnswerRow

import java.time.LocalDate

class AnswerRowConverter @Inject()(checkAnswersFormatters: CheckAnswersFormatters) {

  def bind(userAnswers: UserAnswers, name: String)
          (implicit messages: Messages): Bound = new Bound(userAnswers, name)

  class Bound(userAnswers: UserAnswers, name: String)
             (implicit messages: Messages) {

    def stringQuestion(query: Gettable[String],
                       labelKey: String,
                       changeUrl: String): Option[AnswerRow] = {
      val format = (x: String) => HtmlFormat.escape(x)
      question(query, labelKey, format, changeUrl)
    }

    def numberQuestion[T <: AnyVal](query: Gettable[T],
                                    labelKey: String,
                                    changeUrl: String)
                                   (implicit reads: Reads[T]): Option[AnswerRow] = {
      val format = (x: T) => HtmlFormat.escape(x.toString)
      question(query, labelKey, format, changeUrl)
    }

    def yesNoQuestion(query: Gettable[Boolean],
                      labelKey: String,
                      changeUrl: String): Option[AnswerRow] = {
      val format = (x: Boolean) => yesOrNo(x)
      question(query, labelKey, format, changeUrl)
    }

    def addressQuestion[T <: Address](query: Gettable[T],
                                      labelKey: String,
                                      changeUrl: String)
                                     (implicit reads: Reads[T]): Option[AnswerRow] = {
      val format = (x: T) => checkAnswersFormatters.addressFormatter(x)
      question(query, labelKey, format, changeUrl)
    }

    def currencyQuestion(query: Gettable[Long],
                         labelKey: String,
                         changeUrl: String): Option[AnswerRow] = {
      val format = (x: Long) => currency(x.toString)
      question(query, labelKey, format, changeUrl)
    }

    def dateQuestion(query: Gettable[LocalDate],
                     labelKey: String,
                     changeUrl: String): Option[AnswerRow] = {
      val format = (x: LocalDate) => checkAnswersFormatters.formatDate(x)
      question(query, labelKey, format, changeUrl)
    }

    def assetTypeQuestion(index: Int): Option[AnswerRow] = {
      val format = (x: WhatKindOfAsset) => formatEnum("whatKindOfAsset", x)
      question(
        WhatKindOfAssetPage,
        s"whatKindOfAsset",
        format,
        WhatKindOfAssetController.onPageLoad().url
      )
    }

    def shareClassQuestion(query: Gettable[ShareClass],
                           labelKey: String,
                           changeUrl: String): Option[AnswerRow] = {
      val format = (x: ShareClass) => formatEnum("shares.class", x)
      question(query, labelKey, format, changeUrl)
    }

    def countryQuestion(query: Gettable[String],
                        labelKey: String,
                        changeUrl: String): Option[AnswerRow] = {
      val format = (x: String) => HtmlFormat.escape(checkAnswersFormatters.country(x))
      question(query, labelKey, format, changeUrl)
    }

    private def question[T](query: Gettable[T],
                            labelKey: String,
                            format: T => Html,
                            changeUrl: String)
                           (implicit rds: Reads[T]): Option[AnswerRow] = {
      userAnswers.get(query) map { x =>
        AnswerRow(
          label = messages(s"$labelKey.checkYourAnswersLabel", name),
          answer = format(x),
          changeUrl = changeUrl
        )
      }
    }

  }

}
