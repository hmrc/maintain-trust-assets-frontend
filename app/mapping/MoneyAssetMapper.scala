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

package mapping

import javax.inject.Inject
import mapping.reads.MoneyAsset
import models.{AssetMonetaryAmount, UserAnswers}

import scala.util.Try

class MoneyAssetMapper @Inject() extends Mapping[List[AssetMonetaryAmount]]{
  override def build(userAnswers: UserAnswers): Option[List[AssetMonetaryAmount]] = {

    val assets : List[MoneyAsset] =
      userAnswers.get(mapping.reads.Assets)
        .getOrElse(List.empty[mapping.reads.Asset])
        .collect { case x : MoneyAsset => x }

    assets match {
      case Nil => None
      case list =>
        Some(
          list.flatMap {
            x =>
              Try(x.value.toLong).toOption.map(AssetMonetaryAmount(_))
          }
        )
    }
  }
}
