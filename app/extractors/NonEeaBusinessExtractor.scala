/*
 * Copyright 2024 HM Revenue & Customs
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

import models.{NonUkAddress, UkAddress, UserAnswers}
import models.assets.NonEeaBusinessType
import pages.QuestionPage
import pages.asset.noneeabusiness.add.StartDatePage
import pages.asset.noneeabusiness.amend.IndexPage
import pages.asset.noneeabusiness.{GoverningCountryPage, NamePage, NonUkAddressPage, UkAddressPage, UkAddressYesNoPage}
import play.api.libs.json.JsPath

import scala.util.Try

class NonEeaBusinessExtractor extends AssetExtractor[NonEeaBusinessType] {

  override def apply(answers: UserAnswers,
                     noneEaBusiness: NonEeaBusinessType,
                     index: Int): Try[UserAnswers] = {

    super.apply(answers, noneEaBusiness, index)
      .flatMap(_.set(NamePage(index), noneEaBusiness.orgName))
      .flatMap(_.set(GoverningCountryPage(index), noneEaBusiness.govLawCountry))
      .flatMap(answers => extractAddress(Some(noneEaBusiness.address), answers))
      .flatMap(_.set(StartDatePage(index), noneEaBusiness.startDate))
  }

  override def namePage: QuestionPage[String] = NamePage(0)

  override def ukAddressYesNoPage: QuestionPage[Boolean] = UkAddressYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = UkAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = NonUkAddressPage(0)

  override def startDatePage: QuestionPage[LocalDate] = StartDatePage(0)

  override def indexPage: QuestionPage[Int] = IndexPage

  override def basePath: JsPath = pages.asset.noneeabusiness.basePath
}
