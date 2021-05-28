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

package extractors

import models.assets.PropertyLandType
import models.{NonUkAddress, UkAddress, UserAnswers}
import pages.QuestionPage
import pages.asset.property_or_land.amend.IndexPage
import pages.asset.property_or_land._
import play.api.libs.json.JsPath

import scala.util.Try

class PropertyOrLandExtractor extends AssetExtractor[PropertyLandType] {

  override def apply(answers: UserAnswers,
                     propertyLandType: PropertyLandType,
                     index: Int): Try[UserAnswers] = {

    super.apply(answers, propertyLandType, index)
      .flatMap(_.set(PropertyOrLandAddressYesNoPage, propertyLandType.address.isDefined))
      .flatMap(_.set(PropertyOrLandDescriptionPage, propertyLandType.buildingLandName))
      .flatMap(answers => extractAddress(propertyLandType.address, answers))
      .flatMap(_.set(PropertyOrLandTotalValuePage, propertyLandType.valueFull))
      .flatMap(_.set(TrustOwnAllThePropertyOrLandPage, doesTrustOwnAllThePropertyOrLand(propertyLandType)))
      .flatMap(_.set(PropertyLandValueTrustPage, if (doesTrustOwnAllThePropertyOrLand(propertyLandType)) None else propertyLandType.valuePrevious))
  }

  private def doesTrustOwnAllThePropertyOrLand(propertyLandType: PropertyLandType) : Boolean = {
    propertyLandType.valuePrevious match {
      case Some(valuePrevious) => valuePrevious == propertyLandType.valueFull
      case None => true
    }
  }

  override def ukAddressYesNoPage: QuestionPage[Boolean] = PropertyOrLandAddressUkYesNoPage
  override def ukAddressPage: QuestionPage[UkAddress] = PropertyOrLandUKAddressPage
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = PropertyOrLandInternationalAddressPage

  override def indexPage: QuestionPage[Int] = IndexPage

  override def basePath: JsPath = pages.asset.noneeabusiness.basePath
}
