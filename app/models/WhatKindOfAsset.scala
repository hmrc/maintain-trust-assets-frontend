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

import models.Constants._
import models.WhatKindOfAsset.prefix
import models.assets.Assets
import play.api.i18n.Messages
import viewmodels._

sealed trait WhatKindOfAsset {
  implicit class AssetLabel(asset: WhatKindOfAsset) {
    def label(implicit messages: Messages): String = messages(s"$prefix.$asset")
  }
}

object WhatKindOfAsset extends Enumerable.Implicits {

  case object Money extends WithName("Money") with WhatKindOfAsset
  case object PropertyOrLand extends WithName("PropertyOrLand") with WhatKindOfAsset
  case object Shares extends WithName("Shares") with WhatKindOfAsset
  case object Business extends WithName("Business") with WhatKindOfAsset
  case object Partnership extends WithName("Partnership") with WhatKindOfAsset
  case object Other extends WithName("Other") with WhatKindOfAsset
  case object NonEeaBusiness extends WithName("NonEeaBusiness") with WhatKindOfAsset

  val values: List[WhatKindOfAsset] = List(
    Money, PropertyOrLand, Shares, Business, NonEeaBusiness, Partnership, Other
  )

  val prefix: String = "whatKindOfAsset"

  case class OptionAndLimit(kind: WhatKindOfAsset, limit: Int)

  private val maximumDataSet : Set[OptionAndLimit] = Set(
    OptionAndLimit(Money, MAX_MONEY_ASSETS),
    OptionAndLimit(PropertyOrLand, MAX_PROPERTY_OR_LAND_ASSETS),
    OptionAndLimit(Shares, MAX_SHARES_ASSETS),
    OptionAndLimit(Business, MAX_BUSINESS_ASSETS),
    OptionAndLimit(Partnership, MAX_PARTNERSHIP_ASSETS),
    OptionAndLimit(Other, MAX_OTHER_ASSETS),
    OptionAndLimit(NonEeaBusiness, MAX_NON_EEA_BUSINESS_ASSETS)
  )

  def options(kindsOfAsset: List[WhatKindOfAsset] = values): List[RadioOption] = kindsOfAsset.map {
    value =>
      RadioOption(prefix, value.toString)
  }

  implicit val enumerable: Enumerable[WhatKindOfAsset] =
    Enumerable(values.map(v => v.toString -> v): _*)

  def nonMaxedOutOptions(assets: Assets): List[RadioOption] = {

    def isMaxed(option: WhatKindOfAsset, size: Int) : Boolean = {
      val definedLimit = maximumDataSet.filter(_.kind == option).head
      size >= definedLimit.limit
    }

    val filtered = values.filterNot(x => isMaxed(x, assets.sizeForKind(x)))

    options(filtered)
  }
}
