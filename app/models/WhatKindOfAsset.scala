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

package models

import viewmodels._

sealed trait WhatKindOfAsset

object WhatKindOfAsset extends Enumerable.Implicits {

  case object Money extends WithName("Money") with WhatKindOfAsset
  case object PropertyOrLand extends WithName("PropertyOrLand") with WhatKindOfAsset
  case object Shares extends WithName("Shares") with WhatKindOfAsset
  case object Business extends WithName("Business") with WhatKindOfAsset
  case object Partnership extends WithName("Partnership") with WhatKindOfAsset
  case object Other extends WithName("Other") with WhatKindOfAsset

  val values: List[WhatKindOfAsset] = List(
    Money, PropertyOrLand, Shares, Business, Partnership, Other
  )

  def options(kindsOfAsset: List[WhatKindOfAsset] = values): List[RadioOption] = kindsOfAsset.map {
    value =>
      RadioOption("whatKindOfAsset", value.toString)
  }

  implicit val enumerable: Enumerable[WhatKindOfAsset] =
    Enumerable(values.map(v => v.toString -> v): _*)

  type AssetTypeCount = (WhatKindOfAsset, Int)

  def nonMaxedOutOptions(assets: List[AssetViewModel], assetTypeAtIndex: Option[WhatKindOfAsset]): List[RadioOption] = {
    val assetTypeCounts: List[AssetTypeCount] = List(
      (Money, assets.count(_.isInstanceOf[MoneyAssetViewModel])),
      (PropertyOrLand, assets.count(_.isInstanceOf[PropertyOrLandAssetViewModel])),
      (Shares, assets.count(_.isInstanceOf[ShareAssetViewModel])),
      (Business, assets.count(_.isInstanceOf[BusinessAssetViewModel])),
      (Partnership, assets.count(_.isInstanceOf[PartnershipAssetViewModel])),
      (Other, assets.count(_.isInstanceOf[OtherAssetViewModel]))
    )

    def meetsLimitConditions(assetTypeCount: AssetTypeCount): Boolean = {

      val moneyAssetLimit: Int = 1
      val nonMoneyAssetLimit: Int = 10

      def meetsCondition(maxLimit: Int, assetType: WhatKindOfAsset): Boolean = {
        (assetTypeCount._2 < maxLimit || assetTypeAtIndex.contains(assetType)) && assetTypeCount._1 == assetType
      }

      val meetsMoneyAssetConditions: Boolean = meetsCondition(moneyAssetLimit, Money)

      val meetsNonMoneyAssetsConditions: Boolean = values.filterNot(_ == Money).foldLeft(false)((conditionAlreadyMet, assetType) => {
        meetsCondition(nonMoneyAssetLimit, assetType) || conditionAlreadyMet
      })

      meetsMoneyAssetConditions || meetsNonMoneyAssetsConditions
    }

    options(assetTypeCounts.filter(meetsLimitConditions).map(_._1))
  }
}
