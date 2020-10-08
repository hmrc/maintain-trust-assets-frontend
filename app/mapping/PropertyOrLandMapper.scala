/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject
import mapping.reads.PropertyOrLandAsset
import models.{PropertyLandType, UserAnswers}

import scala.util.Try

class PropertyOrLandMapper @Inject()(addressMapper: AddressMapper) extends Mapping[List[PropertyLandType]]{
    override def build(userAnswers: UserAnswers): Option[List[PropertyLandType]] = {

      val assets : List[PropertyOrLandAsset] =
        userAnswers.get(mapping.reads.Assets)
          .getOrElse(List.empty[mapping.reads.Asset])
          .collect { case x : PropertyOrLandAsset => x }

      assets match {
        case Nil => None
        case list =>
          Some(
            list.flatMap {
              x =>
                for {
                  value <- Try(x.propertyLandValueTrust.map(_.toLong)).toOption
                  totalValue <- Try(x.propertyOrLandTotalValue.toLong).toOption
                } yield {
                  value match {
                    case Some(v) =>
                      PropertyLandType(x.propertyOrLandDescription, addressMapper.build(x.address), totalValue, v)
                    case None =>
                      PropertyLandType(x.propertyOrLandDescription, addressMapper.build(x.address), totalValue, totalValue)
                  }
                }
            }
          )
      }
    }
  }