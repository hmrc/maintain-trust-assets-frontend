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

package extractors

import java.time.LocalDate

import models.assets.AssetType
import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import pages.{EmptyPage, QuestionPage}
import play.api.libs.json.JsPath

import scala.util.{Success, Try}

trait AssetExtractor[T <: AssetType] {

  def apply(answers: UserAnswers, asset: T, index: Int): Try[UserAnswers] = {
    answers.deleteAtPath(basePath)
      .flatMap(_.set(indexPage, index))
  }

  def namePage: QuestionPage[String] = new EmptyPage[String]

  def ukAddressYesNoPage: QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukAddressPage: QuestionPage[UkAddress] = new EmptyPage[UkAddress]
  def nonUkAddressPage: QuestionPage[NonUkAddress] = new EmptyPage[NonUkAddress]

  def startDatePage: QuestionPage[LocalDate] = new EmptyPage[LocalDate]

  def indexPage: QuestionPage[Int] = new EmptyPage[Int]

  def basePath: JsPath

  def extractAddress(address: Option[Address], answers: UserAnswers): Try[UserAnswers] = {
      address match {
        case Some(uk: UkAddress) => answers
          .set(ukAddressYesNoPage, true)
          .flatMap(_.set(ukAddressPage, uk))
        case Some(nonUk: NonUkAddress) => answers
          .set(ukAddressYesNoPage, false)
          .flatMap(_.set(nonUkAddressPage, nonUk))
      }
  }

}
