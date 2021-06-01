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

import mapping.reads.BusinessAsset
import javax.inject.Inject
import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import models.assets.BusinessAssetType
import pages.asset.business._
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.libs.functional.syntax._

class BusinessAssetMapper @Inject()(addressMapper: AddressMapper) extends Mapping[BusinessAssetType, BusinessAsset] with Logging {

  def apply(answers: UserAnswers): Option[BusinessAssetType] = {
    val readFromUserAnswers: Reads[BusinessAssetType] =
      (
        BusinessNamePage.path.read[String] and
          BusinessDescriptionPage.path.read[String] and
          BusinessAddressUkYesNoPage.path.read[Boolean].flatMap {
            case true => BusinessUkAddressPage.path.read[UkAddress].widen[Address]
            case false => BusinessInternationalAddressPage.path.read[NonUkAddress].widen[Address]
          } and
          BusinessValuePage.path.read[Long]
        ) (BusinessAssetType.apply _)

    answers.data.validate[BusinessAssetType](readFromUserAnswers) match {
      case JsSuccess(value, _) =>
        Some(value)
      case JsError(errors) =>
        logger.error(s"[Identifier: ${answers.identifier}] Failed to rehydrate BusinessAssetType from UserAnswers due to $errors")
        None
    }
  }

  override def mapAssets(assets: List[BusinessAsset]): List[BusinessAssetType] = {
    assets.map(x =>
      BusinessAssetType(
        x.assetName,
        x.assetDescription,
        addressMapper.build(x.address),
        x.currentValue))
  }
}
