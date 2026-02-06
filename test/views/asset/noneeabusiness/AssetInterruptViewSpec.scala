/*
 * Copyright 2026 HM Revenue & Customs
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

package views.asset.noneeabusiness

import views.behaviours.ViewBehaviours
import views.html.asset.noneeabusiness.AssetInterruptView

class AssetInterruptViewSpec extends ViewBehaviours {

  "AddAssetInfoView" must {

    val view = viewFor[AssetInterruptView](Some(emptyUserAnswers))

    val applyView = view.apply()(fakeRequest, messages)

    behave like normalPage(applyView, "assetInterruptPage", ignoreTitle = true)

    behave like pageWithTitleAndCaption(applyView, "assetInterruptPage")

    behave like pageWithGuidance(
      applyView,
      messageKeyPrefix = "assetInterruptPage",
      expectedGuidanceKeys = "paragraph10",
      "bullet11",
      "bullet12",
      "bullet13",
      "bullet14"
    )

    behave like pageWithBackLink(applyView)
  }

}
