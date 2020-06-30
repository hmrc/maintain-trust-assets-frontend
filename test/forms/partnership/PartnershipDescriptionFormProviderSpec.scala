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

package forms.partnership

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class PartnershipDescriptionFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "partnershipDescription.error.required"
  val lengthKey = "partnershipDescription.error.length"
  val invalidFormatKey = "partnershipDescription.error.invalid"

  val maxLength = 56

  val form = new PartnershipDescriptionFormProvider()()

  ".value" must {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like nonEmptyField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(fieldName))
    )

    behave like fieldWithRegexpWithGenerator(
      form,
      fieldName,
      Validation.descriptionRegex,
      generator = stringsWithMaxLength(maxLength),
      error = FormError(fieldName, invalidFormatKey, Seq(Validation.descriptionRegex))
    )

  }
}
