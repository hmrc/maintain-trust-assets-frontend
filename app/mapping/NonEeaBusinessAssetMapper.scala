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


import java.time.LocalDate

import mapping.reads.NonEeaBusinessAsset
import models.{Address, UserAnswers}
import javax.inject.Inject
import models.assets.{AddressType, NonEeaBusinessType}
import pages.asset.noneeabusiness.{GoverningCountryPage, NonUkAddressPage, NamePage, StartDatePage}
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.libs.functional.syntax._

class NonEeaBusinessAssetMapper @Inject()(addressMapper: AddressMapper) extends Mapping[NonEeaBusinessType, NonEeaBusinessAsset] with Logging {

  def apply(answers: UserAnswers): Option[NonEeaBusinessType] = {
    val readFromUserAnswers: Reads[NonEeaBusinessType] =
      (
        Reads(_ => JsSuccess(None)) and
          NamePage.path.read[String] and
          NonUkAddressPage.path.read[AddressType] and
          GoverningCountryPage.path.read[String] and
          StartDatePage.path.read[LocalDate] and
          Reads(_ => JsSuccess(None))
        ) (NonEeaBusinessType.apply _)

    answers.data.validate[NonEeaBusinessType](readFromUserAnswers) match {
      case JsSuccess(value, _) =>
        Some(value)
      case JsError(errors) =>
        logger.error(s"[Identifier: ${answers.identifier}] Failed to rehydrate NonEeaBusinessType from UserAnswers due to $errors")
        None
    }
  }

  override def mapAssets(assets: List[NonEeaBusinessAsset]): List[NonEeaBusinessType] = {
    assets.map(x =>
      NonEeaBusinessType(
        lineNo = None,
        orgName = x.name,
        address = addressMapper.build(x.address),
        govLawCountry = x.governingCountry,
        startDate = x.startDate,
        endDate = None
      )
    )
  }
}
