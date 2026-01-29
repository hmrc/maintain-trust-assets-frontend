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

package views.asset.other

import forms.ValueFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.LongViewBehaviours
import views.html.asset.other.OtherAssetValueView

class OtherAssetValueViewSpec extends LongViewBehaviours {

  private val prefix: String      = "other.value"
  private val hintKey: String     = s"$prefix.hint"
  private val description: String = "Description"

  override val form: Form[Long] = new ValueFormProvider(frontendAppConfig).withConfig(prefix)

  "OtherAssetValue view" must {

    val view = viewFor[OtherAssetValueView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, index, NormalMode, description)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), prefix, description)

    behave like pageWithBackLink(applyView(form))

    behave like longPageWithDynamicTitle(form, applyView, prefix, description, Some(hintKey))

    behave like pageWithASubmitButton(applyView(form))

  }

}
