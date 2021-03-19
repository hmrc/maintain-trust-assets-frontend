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

package views.asset.partnership

import forms.StartDateFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.asset.partnership.PartnershipStartDateView

import java.time.LocalDate

class PartnershipStartDateViewSpec extends QuestionViewBehaviours[LocalDate] {

  private val messageKeyPrefix: String = "partnership.startDate"

  override val form: Form[LocalDate] = new StartDateFormProvider(frontendAppConfig).withPrefix(messageKeyPrefix)

  "PartnershipStartDateView view" must {

    val view = viewFor[PartnershipStartDateView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, 0, fakeDraftId)(fakeRequest, messages)

    val applyViewF = (form : Form[_]) => applyView(form)

    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithDateFields(form, applyViewF, messageKeyPrefix, "value")

    behave like pageWithBackLink(applyView(form))
  }
}
