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
import mapping.reads.{Asset, ShareNonPortfolioAsset, SharePortfolioAsset}
import models.WhatKindOfAsset.Shares
import models.{ShareClass, SharesType, UserAnswers}

class ShareAssetMapper @Inject() extends Mapping[List[SharesType]]{
  override def build(userAnswers: UserAnswers): Option[List[SharesType]] = {

    val shares: List[Asset] =
      userAnswers.get(mapping.reads.Assets)
        .getOrElse(List.empty[mapping.reads.Asset])
        .filter(_.whatKindOfAsset == Shares)

    shares match {
      case Nil => None
      case list =>
        Some(
          list.flatMap {
            case x : ShareNonPortfolioAsset =>
              Some(SharesType(x.quantityInTheTrust, x.shareCompanyName, ShareClass.toDES(x.`class`), x.quoted, x.value.toLong))
            case x : SharePortfolioAsset =>
              Some(SharesType(x.quantityInTheTrust, x.name, ShareClass.toDES(ShareClass.Other), x.quoted, x.value.toLong))
            case _ => None
          }
        )
    }
  }
}
