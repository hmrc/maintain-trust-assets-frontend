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
import models.{Assets, UserAnswers}
import play.api.Logger

class AssetMapper @Inject()(moneyAssetMapper: MoneyAssetMapper, shareAssetMapper: ShareAssetMapper,
                            propertyOrLandMapper: PropertyOrLandMapper) extends Mapping[Assets] {

  override def build(userAnswers: UserAnswers): Option[Assets] = {

    val money = moneyAssetMapper.build(userAnswers)
    val shares = shareAssetMapper.build(userAnswers)
    val propertyOrLand = propertyOrLandMapper.build(userAnswers)

    (money, shares, propertyOrLand) match {
      case (None, None, None) =>
        Logger.info(s"[AssetMapper][build] unable to map assets")
        None
      case (_, _, _) =>
        Some(
          Assets(monetary = money, propertyOrLand = propertyOrLand, shares = shares, business = None, partnerShip = None, other = None)
        )
    }
  }
}
