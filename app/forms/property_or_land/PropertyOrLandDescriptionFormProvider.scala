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

package forms.property_or_land

import forms.Validation
import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class PropertyOrLandDescriptionFormProvider @Inject() extends Mappings {

  val propertyOrLandDescriptionMaxLength = 56

  def apply(): Form[String] =
    Form(
      "value" -> text("propertyOrLandDescription.error.required")
        .verifying(
          firstError(
            isNotEmpty("value", "propertyOrLandDescription.error.required"),
            maxLength(propertyOrLandDescriptionMaxLength, "propertyOrLandDescription.error.length"),
            regexp(Validation.descriptionRegex, "propertyOrLandDescription.error.invalid")
          )
        )
    )
}
