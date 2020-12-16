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

import models.UserAnswers
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

trait PrintHelper {

  def checkDetailsSection(userAnswers: UserAnswers,
                          arg: String = "",
                          index: Int,
                          draftId: String)
                         (implicit messages: Messages): Seq[AnswerSection] = {

    Seq(section(userAnswers, arg, index, draftId, None))
  }

  def headingKey(index: Int)(implicit messages: Messages): String

  def printSection(userAnswers: UserAnswers,
                   arg: String = "",
                   index: Int,
                   specificIndex: Int,
                   draftId: String)
                  (implicit messages: Messages): AnswerSection = {

    section(userAnswers, arg, index, draftId, Some(headingKey(specificIndex)))
  }

  private def section(userAnswers: UserAnswers,
              arg: String,
              index: Int,
              draftId: String,
              headingKey: Option[String])
             (implicit messages: Messages): AnswerSection = {

    AnswerSection(
      headingKey = headingKey match {
        case Some(key) => Some(messages(key))
        case _ => None
      },
      rows = answerRows(userAnswers, arg, index, draftId)
    )
  }

  def answerRows(userAnswers: UserAnswers,
                 arg: String,
                 index: Int,
                 draftId: String)
                (implicit messages: Messages): Seq[AnswerRow]

}
