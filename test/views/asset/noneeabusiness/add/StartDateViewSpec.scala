/*
 * Copyright 2025 HM Revenue & Customs
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

package views.asset.noneeabusiness.add

import forms.StartDateFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.asset.noneeabusiness.add.StartDateView

import java.time.LocalDate

class StartDateViewSpec extends QuestionViewBehaviours[LocalDate] {

  private val messageKeyPrefix: String = "nonEeaBusiness.startDate"

  override val form: Form[LocalDate]   =
    new StartDateFormProvider(frontendAppConfig).withConfig(messageKeyPrefix, LocalDate.now())

  private val name: String             = "Test"

  "StartDateView view" must {

    val view = viewFor[StartDateView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(index, form, name)(fakeRequest, messages)

    val applyViewF = (form: Form[_]) => applyView(form)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithDateFields(form, applyViewF, messageKeyPrefix, "value")

    behave like pageWithASubmitButton(applyView(form))
  }

}
