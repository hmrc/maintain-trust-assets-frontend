/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig
import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class ValueFormProvider @Inject()(config: FrontendAppConfig) extends Mappings {

  def withConfig(prefix: String,
                 minValue: Option[Long] = None,
                 maxValue: Option[Long] = None): Form[Long] =
    Form(
      "value" -> longValue(
        prefix = prefix,
        minValue = minValue.getOrElse(config.assetValueLowerLimitExclusive),
        maxValue = maxValue.getOrElse(config.assetValueUpperLimitExclusive),
        minValueKey = if (minValue.isEmpty) s"$prefix.error.zero" else s"$prefix.error.lessThanValueInTrust",
        maxValueKey = if (maxValue.isEmpty) s"$prefix.error.length" else s"$prefix.error.moreThanTotal"
      )
    )
}
