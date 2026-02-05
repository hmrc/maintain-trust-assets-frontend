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

package views.asset.property_or_land

import forms.UKAddressFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.UkAddressViewBehaviours
import views.html.asset.property_or_land.PropertyOrLandUKAddressView

class PropertyOrLandUKAddressViewSpec extends UkAddressViewBehaviours {

  private val messageKeyPrefix = "propertyOrLand.ukAddress"
  private val name             = "Test"

  override val form = new UKAddressFormProvider()()

  "PropertyOrLandUKAddress view" must {

    val view = viewFor[PropertyOrLandUKAddressView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, index, NormalMode)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, name)

    behave like pageWithBackLink(applyView(form))

    behave like ukAddressPage(applyView, Some(messageKeyPrefix), name)

    behave like pageWithASubmitButton(applyView(form))
  }

}
