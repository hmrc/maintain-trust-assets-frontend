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

import mapping.reads.{Asset, Assets}
import models.UserAnswers

import scala.reflect.ClassTag

abstract class Mapping[T, A <: Asset : ClassTag] {

  def build(userAnswers: UserAnswers): Option[T] = {
    assets(userAnswers) match {
      case Nil => None
      case list => Some(mapAssets(list))
    }
  }

  def mapAssets(assets: List[A]): T

  private def assets(userAnswers: UserAnswers): List[A] = {
    val runtimeClass = implicitly[ClassTag[A]].runtimeClass

    userAnswers.get(Assets).getOrElse(Nil).collect {
      case x: A if runtimeClass.isInstance(x) => x
    }
  }
}
