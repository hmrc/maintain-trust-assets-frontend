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
import models.assets.BusinessAssetType
import pages.asset.business._
import play.api.libs.json.Reads
import play.api.libs.functional.syntax._

class BusinessAssetMapper extends Mapper[BusinessAssetType] {

  def apply(answers: UserAnswers): Option[BusinessAssetType] = {
    val readFromUserAnswers: Reads[BusinessAssetType] =
      (
        BusinessNamePage(0).path.read[String] and
          BusinessDescriptionPage(0).path.read[String] and
          BusinessAddressUkYesNoPage(0).path.read[Boolean].flatMap {
            case true => BusinessUkAddressPage(0).path.read[UkAddress].widen[Address]
            case false => BusinessInternationalAddressPage(0).path.read[NonUkAddress].widen[Address]
          } and
          BusinessValuePage(0).path.read[Long]
        ) (BusinessAssetType.apply _)

    mapAnswersWithExplicitReads(answers, readFromUserAnswers)
  }
}
