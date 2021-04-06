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

import java.time.LocalDate

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

case class RemoveAsset(`type`: AssetNameType, index: Int, endDate: LocalDate)

object RemoveAsset {

  implicit val writes : Writes[RemoveAsset] =
    (
      (JsPath \ "type").write[AssetNameType](AssetNameType.writesToTrusts) and
        (JsPath \ "index").write[Int] and
        (JsPath \ "endDate").write[LocalDate]
      ).apply(unlift(RemoveAsset.unapply))

  def apply(`type`: AssetNameType, index: Int): RemoveAsset =  RemoveAsset(`type`, index, LocalDate.now)
}
