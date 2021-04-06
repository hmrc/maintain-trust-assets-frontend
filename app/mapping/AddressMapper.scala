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

import models.assets.AddressType
import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import pages.QuestionPage

class AddressMapper  {

  def build(userAnswers: UserAnswers,
            isUk: QuestionPage[Boolean],
            ukAddress: QuestionPage[UkAddress],
            internationalAddress: QuestionPage[NonUkAddress]) : Option[AddressType] = {

    userAnswers.get(isUk) flatMap {
      uk =>
        if(uk) {
          buildUkAddress(userAnswers.get(ukAddress))
        } else {
          buildInternationalAddress(userAnswers.get(internationalAddress))
        }
    }
  }

  private def buildUkAddress(address: Option[UkAddress]): Option[AddressType] = {
    address.map { x =>
      buildUkAddress(x)
    }
  }

  private def buildUkAddress(address: UkAddress): AddressType = {
    AddressType(
      address.line1,
      address.line2,
      address.line3,
      address.line4,
      Some(address.postcode),
      "GB"
    )
  }

  private def buildInternationalAddress(address: Option[NonUkAddress]): Option[AddressType] = {
    address.map { x =>
      buildInternationalAddress(x)
    }
  }

  private def buildInternationalAddress(address: NonUkAddress): AddressType = {
    AddressType(
      address.line1,
      address.line2,
      address.line3,
      None,
      None,
      address.country
    )
  }

  def buildOptional(address: Address) : Option[AddressType] = {
    address match {
      case a: UkAddress =>
        buildUkAddress(Some(a))
      case a: NonUkAddress =>
        buildInternationalAddress(Some(a))
    }
  }

  def build(address: Address) : AddressType = {
    address match {
      case a: UkAddress =>
        buildUkAddress(a)
      case a: NonUkAddress =>
        buildInternationalAddress(a)
    }
  }

  def build(ukOrInternationalAddress : Option[Address]): Option[AddressType] = {
    ukOrInternationalAddress flatMap {
      case ukAddress : UkAddress => buildUkAddress(Some(ukAddress))
      case international : NonUkAddress => buildInternationalAddress(Some(international))
    }
  }
}
