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

import views.behaviours.ViewBehaviours
import views.html.asset.NonTaxableInfoView

class NonTaxableInfoViewSpec extends ViewBehaviours {

  "NonTaxableInfoView" when {

    val view = viewFor[NonTaxableInfoView](Some(emptyUserAnswers))

    val applyView = view.apply()(fakeRequest, messages)

    behave like normalPage(applyView, "assetInterruptPage.nonTaxable", ignoreTitle = true)

    behave like pageWithTitleAndCaption(applyView, "assetInterruptPage.nonTaxable")

    behave like pageWithGuidance(applyView,
      messageKeyPrefix = "assetInterruptPage.nonTaxable",
      expectedGuidanceKeys = "paragraph1",
      "bullet1",
      "bullet2",
      "bullet3",
      "bullet4"
    )

    behave like pageWithBackLink(applyView)
  }
}
