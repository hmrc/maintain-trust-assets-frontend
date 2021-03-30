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

package models.assets

import models.Address
import java.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class NonEEABusinessAsset(lineNo: String, orgName: String, address: Option[Address], govLawCountry: String, startDate: LocalDate) extends Asset

object NonEEABusinessAsset {

  implicit val reads: Reads[NonEEABusinessAsset] =
    ((__ \ 'lineNo).read[String] and
      (__ \ 'orgName).read[String] and
      __.lazyRead(readNullableAtSubPath[Address](__ \ 'address)) and
      (__ \ "govLawCountry").read[String] and
      (__ \ "startDate").read[LocalDate]).tupled.map {

      case (lineNo, orgName, address, govLawCountry, startDate) =>
        NonEEABusinessAsset(lineNo, orgName, address, govLawCountry, startDate)
    }

  implicit val writes: Writes[NonEEABusinessAsset] =
    ((__ \ 'lineNo).write[String] and
      (__ \ 'orgName).write[String] and
      (__ \ 'address).writeNullable[Address] and
      (__ \ 'govLawCountry).write[String] and
      (__ \ "startDate").write[LocalDate]
      ).apply(unlift(NonEEABusinessAsset.unapply))

  def readNullableAtSubPath[T:Reads](subPath : JsPath) : Reads[Option[T]] = Reads (
    _.transform(subPath.json.pick)
      .flatMap(_.validate[T])
      .map(Some(_))
      .recoverWith(_ => JsSuccess(None))
  )
}