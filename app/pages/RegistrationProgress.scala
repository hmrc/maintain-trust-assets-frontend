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

package pages

import models.AddAssets.NoComplete
import models.Status._
import models.{Status, UserAnswers}
import pages.asset.{AddAssetsPage, TrustOwnsNonEeaBusinessYesNoPage}

import javax.inject.Inject

class RegistrationProgress @Inject()() {

  private def determineStatus(complete: Boolean): Option[Status] = {
    if (complete) {
      Some(Completed)
    } else {
      Some(InProgress)
    }
  }

  def assetsStatus(userAnswers: UserAnswers): Option[Status] = {
    val assets = userAnswers.get(sections.Assets).getOrElse(List.empty)
    val noAssetsInProgress = !assets.exists(_.status == InProgress)
    val noMoreToAdd = (userAnswers.get(AddAssetsPage), userAnswers.get(TrustOwnsNonEeaBusinessYesNoPage)) match {
      case (Some(NoComplete), _) | (_, Some(false)) => true
      case _ => false
    }

    assets match {
      case Nil if userAnswers.isTaxable => None
      case _ => determineStatus(noAssetsInProgress && noMoreToAdd)
    }
  }

}
