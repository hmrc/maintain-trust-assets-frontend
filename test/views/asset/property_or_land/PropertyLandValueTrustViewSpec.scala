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

package views.asset.property_or_land

import forms.ValueFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.LongViewBehaviours
import views.html.asset.property_or_land.PropertyLandValueTrustView

class PropertyLandValueTrustViewSpec extends LongViewBehaviours {

  private val messageKeyPrefix: String = "propertyOrLand.valueInTrust"
  private val maxValue: Long = 100L
  private val index: Int = 0

  override val form: Form[Long] = new ValueFormProvider(frontendAppConfig)
    .withConfig(prefix = messageKeyPrefix, maxValue = Some(maxValue))

  "PropertyLandValueTrust view" must {

    val view = viewFor[PropertyLandValueTrustView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, index, fakeDraftId)(fakeRequest, messages)


    behave like normalPage(applyView(form), messageKeyPrefix)

    behave like pageWithBackLink(applyView(form))

    behave like pageWithASubmitButton(applyView(form))
  }
}
