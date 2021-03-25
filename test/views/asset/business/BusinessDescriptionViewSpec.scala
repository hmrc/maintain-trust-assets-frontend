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

package views.asset.business

import forms.DescriptionFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.asset.buisness.BusinessDescriptionView

class BusinessDescriptionViewSpec extends StringViewBehaviours {

  private val messageKeyPrefix: String = "business.description"
  private val businessName: String = "Test"
  override val form: Form[String] = new DescriptionFormProvider().withConfig(56, messageKeyPrefix)

  private val view: BusinessDescriptionView = viewFor[BusinessDescriptionView](Some(emptyUserAnswers))

  "AssetDescription view" must {

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, businessName)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, businessName)

    behave like pageWithBackLink(applyView(form))

    behave like stringPageWithDynamicTitle(form, applyView, messageKeyPrefix, businessName)

    behave like pageWithASubmitButton(applyView(form))
  }
}
