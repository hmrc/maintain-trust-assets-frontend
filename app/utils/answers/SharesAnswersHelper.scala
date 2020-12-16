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

package utils.answers

import mapping.reads.{Assets, ShareAsset}
import models.UserAnswers
import play.api.i18n.Messages
import utils.print.SharesPrintHelper
import viewmodels.AnswerSection

import javax.inject.Inject

class SharesAnswersHelper @Inject()(printHelper: SharesPrintHelper) {

  def apply(userAnswers: UserAnswers)(implicit messages: Messages): Seq[AnswerSection] = {

    val sharesAssets = userAnswers.get(Assets).getOrElse(Nil).zipWithIndex.collect {
      case (x: ShareAsset, index: Int) => (x, index)
    }

    sharesAssets.zipWithIndex.map {
      case ((vm, index), specificIndex) =>
        printHelper.printSection(
          userAnswers = userAnswers,
          arg = vm.name,
          index = index,
          specificIndex = specificIndex,
          draftId = userAnswers.draftId
        )
    }
  }
}
