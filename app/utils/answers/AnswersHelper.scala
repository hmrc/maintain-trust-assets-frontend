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

package utils.answers

import mapping.reads.{Asset, Assets}
import models.UserAnswers
import play.api.i18n.Messages
import utils.print.PrintHelper
import viewmodels.AnswerSection

import scala.reflect.ClassTag

class AnswersHelper[A <: Asset : ClassTag](printHelper: PrintHelper) {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {

    val runtimeClass = implicitly[ClassTag[A]].runtimeClass

    val assets = userAnswers.get(Assets).getOrElse(Nil).zipWithIndex.collect {
      case (x: A, index: Int) if runtimeClass.isInstance(x) => (x, index)
    }

    assets.zipWithIndex.map {
      case ((asset, index), specificIndex) =>
        printHelper.printSection(
          userAnswers = userAnswers,
          arg = asset.arg,
          index = index,
          specificIndex = specificIndex,
          draftId = userAnswers.draftId
        )
    }
  }
}
