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

import forms.WhatKindOfAssetFormProvider
import models.WhatKindOfAsset
import play.api.Application
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.asset.WhatKindOfAssetView

class WhatKindOfAssetViewSpec extends ViewBehaviours {

  private val messageKeyPrefix: String = "whatKindOfAsset"

  private val form: Form[WhatKindOfAsset] = new WhatKindOfAssetFormProvider()()

  private val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

  private val view: WhatKindOfAssetView = application.injector.instanceOf[WhatKindOfAssetView]

  def applyView(form: Form[_]): HtmlFormat.Appendable =
    view.apply(form, WhatKindOfAsset.options())(fakeRequest, messages)

  "WhatKindOfAssetView" when {

    "rendered" must {

      behave like normalPage(applyView(form), messageKeyPrefix)

      "contain radio buttons for the value" in {

        val doc = asDocument(applyView(form))

        for (option <- WhatKindOfAsset.options()) {
          assertContainsRadioButton(doc, option.id, "value", option.value, isChecked = false)
        }
      }
    }

    for (option <- WhatKindOfAsset.options()) {

      s"rendered with a value of '${option.value}'" must {

        s"have the '${option.value}' radio button selected" in {

          val doc = asDocument(applyView(form.bind(Map("value" -> s"${option.value}"))))

          assertContainsRadioButton(doc, option.id, "value", option.value, isChecked = true)

          for (unselectedOption <- WhatKindOfAsset.options().filterNot(o => o == option)) {
            assertContainsRadioButton(doc, unselectedOption.id, "value", unselectedOption.value, isChecked = false)
          }
        }
      }
    }

    behave like pageWithBackLink(applyView(form))

    behave like pageWithASubmitButton(applyView(form))
  }
}
