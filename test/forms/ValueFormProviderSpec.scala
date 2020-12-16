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

package forms

import forms.behaviours.LongFieldBehaviours
import play.api.data.FormError

class ValueFormProviderSpec extends LongFieldBehaviours {

  private val prefix: String = "propertyOrLand.valueInTrust"
  private val fieldName = "value"
  private val requiredKey = s"$prefix.error.required"
  private val zeroNumberKey = s"$prefix.error.zero"
  private val invalidOnlyNumbersKey = s"$prefix.error.invalid"
  private val invalidWholeNumberKey = s"$prefix.error.wholeNumber"

  "ValueFormProvider" when {

    "max value provided" must {

      val maxValue: Long = 100L
      val maxValueKey = s"$prefix.error.moreThanTotal"

      val form = new ValueFormProvider(frontendAppConfig).withConfig(prefix, maxValue)

      behave like longField(
        form,
        fieldName,
        nonNumericError = FormError(fieldName, invalidOnlyNumbersKey),
        wholeNumberError = FormError(fieldName, invalidWholeNumberKey),
        maxNumberError = FormError(fieldName, maxValueKey),
        zeroError = FormError(fieldName, zeroNumberKey),
        Some(maxValue)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }

    "max value not provided" must {

      val maxValueKey = s"$prefix.error.length"

      val form = new ValueFormProvider(frontendAppConfig).withConfig(prefix)

      behave like longField(
        form,
        fieldName,
        nonNumericError = FormError(fieldName, invalidOnlyNumbersKey),
        wholeNumberError = FormError(fieldName, invalidWholeNumberKey),
        maxNumberError = FormError(fieldName, maxValueKey),
        zeroError = FormError(fieldName, zeroNumberKey),
        None
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )
    }
  }
}
