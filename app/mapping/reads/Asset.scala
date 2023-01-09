/*
 * Copyright 2023 HM Revenue & Customs
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

package mapping.reads

import models.WhatKindOfAsset
import play.api.libs.json.Reads

import scala.language.implicitConversions

trait Asset {
  val whatKindOfAsset: WhatKindOfAsset
  val arg: String = ""
}

object Asset {

  implicit class ReadsWithContravariantOr[A](a: Reads[A]) {

    def or[B >: A](b: Reads[B]): Reads[B] =
      a.map[B](identity).orElse(b)
  }

  implicit def convertToSupertype[A, B >: A](a: Reads[A]): Reads[B] =
    a.map(identity)

  implicit lazy val reads : Reads[Asset] = {
    MoneyAsset.reads or
      PropertyOrLandAsset.reads or
      ShareNonPortfolioAsset.reads or
      SharePortfolioAsset.reads or
      BusinessAsset.reads or
      PartnershipAsset.reads or
      OtherAsset.reads or
      NonEeaBusinessAsset.reads
  }

}