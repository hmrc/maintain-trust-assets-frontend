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

package forms.behaviours

import base.FakeTrustsApp
import play.api.data.{Form, FormError}

trait LongFieldBehaviours extends FieldBehaviours with FakeTrustsApp {

  def longField(form: Form[_],
                fieldName: String,
                nonNumericError: FormError,
                wholeNumberError: FormError,
                maxNumberError: FormError,
                minNumberError: FormError,
                maxValue: Option[Long] = None,
                minValue: Option[Long] = None): Unit = {

    val max: Long = maxValue.getOrElse(frontendAppConfig.assetValueUpperLimitExclusive)
    val min: Long = minValue.getOrElse(frontendAppConfig.assetValueLowerLimitExclusive)

    "not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
          result.errors shouldEqual Seq(nonNumericError)
      }
    }

    "not bind decimals" in {
      forAll(decimals -> "decimal") {
        decimal =>
          val result = form.bind(Map(fieldName -> decimal)).apply(fieldName)
          result.errors shouldEqual Seq(wholeNumberError)
      }
    }

    s"not bind numbers larger than or equal to $max" in {

      forAll(longsLargerThanOrEqualToMaxValue(max) -> "longsLargerThanOrEqualToMax") {
        num: Long =>
          val result = form.bind(Map(fieldName -> num.toString)).apply(fieldName)
          result.errors shouldEqual Seq(maxNumberError)
      }
    }

    s"not bind numbers less than or equal to $min" in {

      forAll(longsLessThan1 -> "longsLessThan1") {
        number: Long =>
          val result = form.bind(Map(fieldName -> number.toString)).apply(fieldName)
          result.errors shouldEqual Seq(minNumberError)
      }
    }
  }
}
