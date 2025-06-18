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

package mapping

import models.UserAnswers
import models.assets.AssetMonetaryAmount
import pages.asset.money._
import play.api.libs.json.Reads

class MoneyAssetMapper extends Mapper[AssetMonetaryAmount] {

  def apply(answers: UserAnswers): Option[AssetMonetaryAmount] = {
    val readFromUserAnswers: Reads[AssetMonetaryAmount] = AssetMoneyValuePage(0).path.read[Long].map(AssetMonetaryAmount.apply)

    mapAnswersWithExplicitReads(answers, readFromUserAnswers)
  }

//  def apply(answers: UserAnswers): Option[MoneyType] = {
//    val readFromUserAnswers: Reads[MoneyType] =
//      AssetMoneyValuePage.path.read[Long].map(MoneyType.apply)
//
//    mapAnswersWithExplicitReads(answers, readFromUserAnswers)
//  }
}
