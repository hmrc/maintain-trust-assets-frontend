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

import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import pages.QuestionPage

class AddressMapper {

  def build(
    userAnswers: UserAnswers,
    isUk: QuestionPage[Boolean],
    ukAddress: QuestionPage[UkAddress],
    internationalAddress: QuestionPage[NonUkAddress]
  ): Option[Address] =

    userAnswers.get(isUk) flatMap { uk =>
      if (uk) {
        buildUkAddress(userAnswers.get(ukAddress))
      } else {
        buildInternationalAddress(userAnswers.get(internationalAddress))
      }
    }

  private def buildUkAddress(address: Option[UkAddress]): Option[Address] =
    address.map { x =>
      buildUkAddress(x)
    }

  private def buildUkAddress(address: UkAddress): Address =
    UkAddress(
      address.line1,
      address.line2,
      address.line3,
      address.line4,
      address.postcode
    )

  private def buildInternationalAddress(address: Option[NonUkAddress]): Option[Address] =
    address.map { x =>
      buildInternationalAddress(x)
    }

  private def buildInternationalAddress(address: NonUkAddress): Address =
    NonUkAddress(
      address.line1,
      address.line2,
      address.line3,
      address.country
    )

  def buildOptional(address: Address): Option[Address] =
    address match {
      case a: UkAddress    =>
        buildUkAddress(Some(a))
      case a: NonUkAddress =>
        buildInternationalAddress(Some(a))
    }

  def build(address: Address): Address =
    address match {
      case a: UkAddress    =>
        buildUkAddress(a)
      case a: NonUkAddress =>
        buildInternationalAddress(a)
    }

  def build(ukOrInternationalAddress: Option[Address]): Option[Address] =
    ukOrInternationalAddress flatMap {
      case ukAddress: UkAddress        => buildUkAddress(Some(ukAddress))
      case international: NonUkAddress => buildInternationalAddress(Some(international))
    }

}
