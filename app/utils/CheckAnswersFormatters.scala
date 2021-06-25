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

package utils

import models.{Address, NonUkAddress, UkAddress}
import play.api.i18n.Messages
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.escape
import uk.gov.hmrc.play.language.LanguageUtils
import utils.countryOptions.CountryOptions

import java.time.LocalDate
import javax.inject.Inject

class CheckAnswersFormatters @Inject()(languageUtils: LanguageUtils,
                                       countryOptions: CountryOptions) {

  def formatDate(date: LocalDate)(implicit messages: Messages): Html = {
    val formattedDate: String = languageUtils.Dates.formatDate(date)
    escape(formattedDate)
  }

  def addressFormatter(address: Address)(implicit messages: Messages): Html = {
    address match {
      case a: UkAddress => ukAddress(a)
      case a: NonUkAddress => internationalAddress(a)
    }
  }

  private def ukAddress(address: UkAddress): Html = {

    val lines: Seq[Html] = Seq(
      Some(escape(address.line1)),
      Some(escape(address.line2)),
      address.line3.map(escape),
      address.line4.map(escape),
      Some(escape(address.postcode))
    ).flatten

    breakLines(lines)
  }

  private def internationalAddress(address: NonUkAddress)(implicit messages: Messages): Html = {

    val lines: Seq[Html] = Seq(
      Some(escape(address.line1)),
      Some(escape(address.line2)),
      address.line3.map(escape),
      Some(escape(country(address.country)))
    ).flatten

    breakLines(lines)
  }

  def country(code: String)(implicit messages: Messages): String = {
    countryOptions.options.find(_.value.equals(code)).map(_.label).getOrElse("")
  }

  private def breakLines(lines: Seq[Html]): Html = {
    Html(lines.mkString("<br />"))
  }
}

object CheckAnswersFormatters {

  def yesOrNo(answer: Boolean)(implicit messages: Messages): Html = {
    if (answer) {
      escape(messages("site.yes"))
    } else {
      escape(messages("site.no"))
    }
  }

  def currency(value: String): Html = escape(currencyFormat(value))

  def currencyFormat(value: String): String = s"£$value"

  def formatEnum[T](key: String, answer: T)(implicit messages: Messages): Html = {
    escape(messages(s"$key.$answer"))
  }
}
