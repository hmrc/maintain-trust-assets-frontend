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

package forms

import config.FrontendAppConfig
import forms.mappings.Mappings
import play.api.data.Form

import java.time.LocalDate
import javax.inject.Inject

class StartDateFormProvider @Inject()(appConfig: FrontendAppConfig) extends Mappings {

  def withPrefix(prefix: String): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "partnership.startDate.error.invalid",
        allRequiredKey = "partnership.startDate.error.required.all",
        twoRequiredKey = "partnership.startDate.error.required.two",
        requiredKey    = "partnership.startDate.error.required"
      ).verifying(firstError(
        maxDate(LocalDate.now, s"partnership.startDate.error.future", "day", "month", "year"),
        minDate(appConfig.minDate, s"partnership.startDate.error.past", "day", "month", "year")
      ))
    )
}
