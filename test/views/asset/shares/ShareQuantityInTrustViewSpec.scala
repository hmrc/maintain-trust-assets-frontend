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

package views.asset.shares

import forms.QuantityFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.LongViewBehaviours
import views.html.asset.shares.ShareQuantityInTrustView

class ShareQuantityInTrustViewSpec extends LongViewBehaviours {

  private val messageKeyPrefix = "shares.quantityInTrust"
  private val companyName = "Company"

  override val form: Form[Long] = new QuantityFormProvider(frontendAppConfig).withPrefix(messageKeyPrefix)

  "ShareQuantityInTrust view" must {

    val view = viewFor[ShareQuantityInTrustView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, NormalMode, companyName)(fakeRequest, messages)

    behave like pageWithBackLink(applyView(form))

    behave like longPageWithDynamicTitle(form, applyView, messageKeyPrefix, companyName, Some(messages(s"$messageKeyPrefix.hint")))

    pageWithASubmitButton(applyView(form))
  }
}
