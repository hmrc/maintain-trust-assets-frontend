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

package viewmodels

import models.Address

trait AssetViewModelReads {

  implicit class OptionalString(s: String) {
    def toOption: Option[String] = if (s.isEmpty) None else Some(s)
  }

  implicit class OptionalAddress[T <: Address](a: T) {
    def toOption: Option[T] = if (a.line1.isEmpty) None else Some(a)
    def toLine1: Option[String] = a.toOption.map(_.line1)
  }

}
