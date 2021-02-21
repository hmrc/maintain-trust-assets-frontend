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

import models.{Address, InternationalAddress, UKAddress}
import org.joda.time.{LocalDate => JodaDate}
import play.api.i18n.Messages
import play.twirl.api.Html
import play.twirl.api.HtmlFormat.escape
import uk.gov.hmrc.play.language.LanguageUtils
import utils.countryOptions.CountryOptions

import java.time.{LocalDate => JavaDate}
import javax.inject.Inject

class CheckAnswersFormatters @Inject()(languageUtils: LanguageUtils,
                                       countryOptions: CountryOptions) {

  def formatDate(date: JavaDate)(implicit messages: Messages): Html = {
    val convertedDate: JodaDate = new JodaDate(date.getYear, date.getMonthValue, date.getDayOfMonth)
    val formattedDate: String = languageUtils.Dates.formatDate(convertedDate)
    escape(formattedDate)
  }

  def yesOrNo(answer: Boolean)(implicit messages: Messages): Html = {
    if (answer) {
      escape(messages("site.yes"))
    } else {
      escape(messages("site.no"))
    }
  }

  def currency(value: String): Html = escape(currencyFormat(value))

  def currencyFormat(value: String): String = s"£$value"

  def addressFormatter(address: Address)(implicit messages: Messages): Html = {
    address match {
      case a: UKAddress => ukAddress(a)
      case a: InternationalAddress => internationalAddress(a)
    }
  }

  private def breakLines(lines: Seq[Html]): Html = {
    Html(lines.mkString("<br />"))
  }

  def formatEnum[T](key: String, answer: T)(implicit messages: Messages): Html = {
    escape(messages(s"$key.$answer"))
  }

  private def ukAddress(address: UKAddress): Html = {

    val lines: Seq[Html] = Seq(
      Some(escape(address.line1)),
      Some(escape(address.line2)),
      address.line3.map(escape),
      address.line4.map(escape),
      Some(escape(address.postcode))
    ).flatten

    breakLines(lines)
  }

  def country(code: String)(implicit messages: Messages): String = {
    countryOptions.options.find(_.value.equals(code)).map(_.label).getOrElse("")
  }

  private def internationalAddress(address: InternationalAddress)(implicit messages: Messages): Html = {

    val lines: Seq[Html] = Seq(
      Some(escape(address.line1)),
      Some(escape(address.line2)),
      address.line3.map(escape),
      Some(escape(country(address.country)))
    ).flatten

    breakLines(lines)
  }
}
