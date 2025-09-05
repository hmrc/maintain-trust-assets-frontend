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

package mapping

import java.time.LocalDate
import models.{Address, UserAnswers}
import models.assets.NonEeaBusinessType
import pages.asset.noneeabusiness.add.StartDatePage
import pages.asset.noneeabusiness.{GoverningCountryPage, NamePage, NonUkAddressPage}
import play.api.libs.json.{JsSuccess, Reads}
import play.api.libs.functional.syntax._

class NonEeaBusinessAssetMapper extends Mapper[NonEeaBusinessType] {

  def apply(answers: UserAnswers): Option[NonEeaBusinessType] = {
    val readFromUserAnswers: Reads[NonEeaBusinessType] =
      (
        Reads(_ => JsSuccess(None)) and
          NamePage(0).path.read[String] and
          NonUkAddressPage(0).path.read[Address] and
          GoverningCountryPage(0).path.read[String] and
          StartDatePage.path.read[LocalDate] and
          Reads(_ => JsSuccess(None)) and
          Reads(_ => JsSuccess(true))
        ) (NonEeaBusinessType.apply _)

    mapAnswersWithExplicitReads(answers, readFromUserAnswers)
  }
}
