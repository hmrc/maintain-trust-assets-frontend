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

import models.{Mode, UserAnswers}
import play.api.i18n.Messages
import viewmodels.{AnswerRow, AnswerSection}

trait PrintHelper {

  def checkDetailsSection(userAnswers: UserAnswers,
                          arg: String = "",
                          mode: Mode)
                         (implicit messages: Messages): Seq[AnswerSection] = {

    Seq(section(
      userAnswers = userAnswers,
      arg = arg,
      mode = mode,
      headingKey = None
    ))
  }

  val assetType: String

  def printSection(userAnswers: UserAnswers,
                   arg: String = "",
                   mode: Mode,
                   specificIndex: Int)
                  (implicit messages: Messages): AnswerSection = {

    section(
      userAnswers = userAnswers,
      arg = arg,
      mode = mode,
      headingKey = Some(messages(s"answerPage.section.$assetType.subheading", specificIndex + 1))
    )
  }

  private def section(userAnswers: UserAnswers,
                      arg: String,
                      mode: Mode,
                      headingKey: Option[String])
                     (implicit messages: Messages): AnswerSection = {

    AnswerSection(
      headingKey = headingKey match {
        case Some(key) => Some(messages(key))
        case _ => None
      },
      rows = answerRows(userAnswers, arg, mode)
    )
  }

  def answerRows(userAnswers: UserAnswers,
                 arg: String,
                 mode: Mode)
                (implicit messages: Messages): Seq[AnswerRow]

}
