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

package mapping

import models.{Assets, UserAnswers}
import play.api.Logging

import javax.inject.Inject

class AssetMapper @Inject()(moneyAssetMapper: MoneyAssetMapper,
                            propertyOrLandMapper: PropertyOrLandMapper,
                            shareAssetMapper: ShareAssetMapper,
                            businessAssetMapper: BusinessAssetMapper,
                            partnershipAssetMapper: PartnershipAssetMapper,
                            otherAssetMapper: OtherAssetMapper,
                            nonEeaBusinessAssetMapper: NonEeaBusinessAssetMapper) extends Logging {

  def build(userAnswers: UserAnswers): Option[Assets] = {

    val money = moneyAssetMapper.build(userAnswers)
    val propertyOrLand = propertyOrLandMapper.build(userAnswers)
    val shares = shareAssetMapper.build(userAnswers)
    val business = businessAssetMapper.build(userAnswers)
    val partnership = partnershipAssetMapper.build(userAnswers)
    val other = otherAssetMapper.build(userAnswers)
    val nonEeaBusiness = nonEeaBusinessAssetMapper.build(userAnswers)

    (money, propertyOrLand, shares, business, partnership, other, nonEeaBusiness) match {
      case (None, None, None, None, None, None, None) =>
        logger.info(s"[build] unable to map assets")
        None
      case _ =>
        Some(
          Assets(
            monetary = money,
            propertyOrLand = propertyOrLand,
            shares = shares,
            business = business,
            partnerShip = partnership,
            other = other,
            nonEeaBusiness = nonEeaBusiness
          )
        )
    }
  }
}
