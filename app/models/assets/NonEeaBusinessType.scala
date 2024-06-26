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

package models.assets

import java.time.LocalDate

import models.Address
import play.api.libs.json.{Format, Json}

final case class NonEeaBusinessType(lineNo: Option[String],
                              orgName: String,
                              address: Address,
                              govLawCountry: String,
                              startDate: LocalDate,
                              endDate: Option[LocalDate],
                              provisional: Boolean) extends AssetType

object NonEeaBusinessType {
  implicit val format: Format[NonEeaBusinessType] = Json.format[NonEeaBusinessType]
}
