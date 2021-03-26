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

package pages.asset

import models.WhatKindOfAsset._
import models.{WhatKindOfAsset, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath

import scala.util.Try

case object AddNowPage extends QuestionPage[WhatKindOfAsset] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "addNow"

  override def cleanup(value: Option[WhatKindOfAsset], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {

      case Some(Money) =>
        userAnswers.deleteAtPath(money.basePath)

      case Some(PropertyOrLand) =>
        userAnswers.deleteAtPath(property_or_land.basePath)

      case Some(Shares) =>
        userAnswers.deleteAtPath(shares.basePath)

      case Some(Business) =>
        userAnswers.deleteAtPath(business.basePath)

      case Some(Partnership) =>
        userAnswers.deleteAtPath(partnerrship.basePath)

      case Some(Other) =>
        userAnswers.deleteAtPath(other.basePath)

      case Some(NonEeaBusiness) =>
        userAnswers.deleteAtPath(noneeabusiness.basePath)

      case _ =>
        super.cleanup(value, userAnswers)
    }
  }

}
