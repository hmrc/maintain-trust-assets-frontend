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

import models.assets.AssetType
import models.{Address, NonUkAddress, UkAddress, UserAnswers}
import pages.{EmptyPage, QuestionPage}
import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, Reads}

import scala.reflect.{ClassTag, classTag}

abstract class Mapper[T <: AssetType : ClassTag] extends Logging {

  def apply(answers: UserAnswers): Option[T] = {
    answers.data.validate[T](reads) match {
      case JsSuccess(value, _) =>
        Some(value)
      case JsError(errors) =>
        logger.error(s"[UTR: ${answers.identifier}] Failed to rehydrate ${classTag[T].runtimeClass.getSimpleName} from UserAnswers due to $errors")
        None
    }
  }

  val reads: Reads[T]

  def ukAddressYesNoPage: QuestionPage[Boolean] = new EmptyPage[Boolean]
  def ukAddressPage: QuestionPage[UkAddress] = new EmptyPage[UkAddress]
  def nonUkAddressPage: QuestionPage[NonUkAddress] = new EmptyPage[NonUkAddress]

  def readAddress: Reads[Option[Address]] = {
    ukAddressYesNoPage.path.readNullable[Boolean].flatMap {
      case Some(true) => ukAddressPage.path.readNullable[UkAddress].widen[Option[Address]]
      case Some(false) => nonUkAddressPage.path.readNullable[NonUkAddress].widen[Option[Address]]
      case _ => Reads(_ => JsSuccess(None)).widen[Option[Address]]
    }
  }

}
