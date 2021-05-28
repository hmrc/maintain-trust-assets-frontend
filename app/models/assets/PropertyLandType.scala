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
import play.api.libs.json.{Format, Json}

final case class PropertyLandType(buildingLandName: Option[String],
                                  address: Option[Address],
                                  valueFull: Long,
                                  valuePrevious: Option[Long]) extends AssetType {

  val description : Option[String] = buildingLandName orElse address.map(_.line1)
  val name: String = description.getOrElse("")
}

object PropertyLandType {
  implicit val propertyLandTypeFormat: Format[PropertyLandType] = Json.format[PropertyLandType]
}
