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

package pages.asset.property_or_land

import models.UserAnswers
import pages.QuestionPage
import play.api.libs.json.JsPath
import sections.Assets

import scala.util.Try

case object PropertyOrLandAddressUkYesNoPage extends QuestionPage[Boolean] {

  override def path: JsPath = Assets.path \ toString

  override def toString: String = "propertyOrLandAddressUkYesNo"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value match {
      case Some(true) =>
        userAnswers.remove(PropertyOrLandInternationalAddressPage)
      case Some(false) =>
        userAnswers.remove(PropertyOrLandUKAddressPage)
      case _ => super.cleanup(value, userAnswers)
    }
}
