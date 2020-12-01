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

package views.asset

import forms.YesNoFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.asset.RemoveAssetYesNoView

class RemoveAssetYesNoViewSpec extends YesNoViewBehaviours {

  private val messageKeyPrefix: String = "assets.removeYesNo"

  override val form: Form[Boolean] = new YesNoFormProvider().withPrefix(messageKeyPrefix)

  "RemoveAssetYesNoView" must {

    val assetLabel: String = "Label"
    val index: Int = 0

    val view = viewFor[RemoveAssetYesNoView](Some(emptyUserAnswers))

    def applyView(form: Form[_]): HtmlFormat.Appendable =
      view.apply(form, fakeDraftId, index, assetLabel)(fakeRequest, messages)

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, assetLabel)

    behave like pageWithBackLink(applyView(form))

    behave like yesNoPage(form, applyView, messageKeyPrefix, Seq(assetLabel))

    behave like pageWithASubmitButton(applyView(form))
  }
}
