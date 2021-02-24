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
import views.html.asset.TaxableInfoView

class TaxableInfoViewSpec extends ViewBehaviours {

  "TaxableInfoView" when {

    "4mld" must {

      val view = viewFor[TaxableInfoView](Some(emptyUserAnswers))

      val applyView = view.apply(fakeDraftId, is5mldEnabled = false)(fakeRequest, messages)

      behave like normalPage(applyView, "assetInterruptPage", ignoreTitle = true)

      behave like pageWithTitleAndCaption(applyView, "assetInterruptPage")

      behave like pageWithGuidance(applyView,
        messageKeyPrefix = "assetInterruptPage",
        expectedGuidanceKeys = "subheading1",
        "paragraph1",
        "subheading2",
        "paragraph2",
        "bullet1",
        "bullet2",
        "bullet3",
        "paragraph3",
        "subheading3",
        "paragraph4",
        "bullet4",
        "bullet5",
        "bullet6",
        "paragraph5",
        "paragraph6",
        "subheading4",
        "paragraph7",
        "bullet7",
        "bullet8",
        "bullet9",
        "bullet10",
        "subheading5",
        "paragraph8",
        "subheading6",
        "paragraph9"
      )

      behave like pageWithBackLink(applyView)
    }

    "5mld" must {

      val view = viewFor[TaxableInfoView](Some(emptyUserAnswers))

      val applyView = view.apply(fakeDraftId, is5mldEnabled = true)(fakeRequest, messages)

      behave like normalPage(applyView, "assetInterruptPage.5mld", ignoreTitle = true)

      behave like pageWithTitleAndCaption(applyView, "assetInterruptPage.5mld")

      behave like pageWithGuidance(applyView,
        messageKeyPrefix = "assetInterruptPage",
        expectedGuidanceKeys = "subheading1",
        "paragraph1",
        "subheading2",
        "paragraph2",
        "bullet1",
        "bullet2",
        "bullet3",
        "paragraph3",
        "subheading3",
        "paragraph4",
        "bullet4",
        "bullet5",
        "bullet6",
        "paragraph5",
        "paragraph6",
        "subheading4",
        "paragraph7",
        "bullet7",
        "bullet8",
        "bullet9",
        "bullet10",
        "subheading5",
        "paragraph8",
        "subheading6",
        "paragraph9"
      )

      behave like pageWithBackLink(applyView)
    }
  }
}
