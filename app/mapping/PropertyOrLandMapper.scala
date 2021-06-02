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

import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import models.assets.PropertyLandType
import pages.asset.property_or_land._
import play.api.libs.json.{JsSuccess, Reads}
import play.api.libs.functional.syntax._

class PropertyOrLandMapper extends Mapper[PropertyLandType] {

  def apply(answers: UserAnswers): Option[PropertyLandType] = {
    val readFromUserAnswers: Reads[PropertyLandType] =
      (
          PropertyOrLandDescriptionPage.path.readNullable[String] and
          PropertyOrLandAddressUkYesNoPage.path.readNullable[Boolean].flatMap {
            case Some(true) => PropertyOrLandUKAddressPage.path.readNullable[UkAddress].widen[Option[Address]]
            case Some(false) => PropertyOrLandInternationalAddressPage.path.readNullable[NonUkAddress].widen[Option[Address]]
            case _ => Reads(_ => JsSuccess(None)).widen[Option[Address]]
          } and
          PropertyOrLandTotalValuePage.path.read[Long] and
          TrustOwnAllThePropertyOrLandPage.path.read[Boolean].flatMap {
            case true => PropertyOrLandTotalValuePage.path.readNullable[Long]
            case false => PropertyLandValueTrustPage.path.readNullable[Long]
          }
        ) (PropertyLandType.apply _)

    mapAnswersWithExplicitReads(answers, readFromUserAnswers)
  }
}