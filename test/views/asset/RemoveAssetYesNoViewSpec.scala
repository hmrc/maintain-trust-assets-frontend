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

package views.asset

import forms.YesNoFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.asset.RemoveAssetYesNoView

class RemoveAssetYesNoViewSpec extends YesNoViewBehaviours {

  val assetLabel: String = "Label"
  val index: Int = 0

  override val form: Form[Boolean] = new YesNoFormProvider().withPrefix("")

  "RemoveAssetYesNoView" when {

    "taxable" must {

      val prefix: String = "assets"
      val messageKeyPrefix: String = s"$prefix.removeYesNo"
      val form: Form[Boolean] = new YesNoFormProvider().withPrefix(messageKeyPrefix)

      val view = viewFor[RemoveAssetYesNoView](Some(emptyUserAnswers.copy(isTaxable = true)))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, fakeDraftId, index, prefix, assetLabel)(fakeRequest, messages)

      behave like dynamicTitlePage(applyView(form), messageKeyPrefix, assetLabel)

      behave like pageWithBackLink(applyView(form))

      behave like yesNoPage(form, applyView, messageKeyPrefix, Seq(assetLabel))

      behave like pageWithASubmitButton(applyView(form))
    }

    "non-taxable" must {

      val prefix: String = "assets.nonTaxable"
      val messageKeyPrefix: String = s"$prefix.removeYesNo"

      val view = viewFor[RemoveAssetYesNoView](Some(emptyUserAnswers.copy(isTaxable = false)))

      def applyView(form: Form[_]): HtmlFormat.Appendable =
        view.apply(form, fakeDraftId, index, prefix, assetLabel)(fakeRequest, messages)

      behave like dynamicTitlePage(applyView(form), messageKeyPrefix, assetLabel)

      behave like pageWithBackLink(applyView(form))

      behave like yesNoPage(form, applyView, messageKeyPrefix, Seq(assetLabel))

      behave like pageWithASubmitButton(applyView(form))
    }
  }
}
