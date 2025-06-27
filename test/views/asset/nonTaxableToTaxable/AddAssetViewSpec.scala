/*
 * Copyright 2024 HM Revenue & Customs
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

package views.asset.nonTaxableToTaxable

import forms.AddAssetsFormProvider
import models.{AddAssets, WhatKindOfAsset}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.AddRow
import views.behaviours.{OptionsViewBehaviours, TabularDataViewBehaviours}
import views.html.asset.nonTaxableToTaxable.AddAssetsView

class AddAssetViewSpec extends OptionsViewBehaviours with TabularDataViewBehaviours {

  private val completeAssets: Seq[AddRow] = Seq(
    AddRow("4500", WhatKindOfAsset.Money.toString, "#", "#"),
    AddRow("4500", WhatKindOfAsset.Money.toString, "#", "#")
  )

  private val messageKeyPrefix: String = "nonTaxableToTaxable.addAssets"

  private val form: Form[AddAssets] = new AddAssetsFormProvider().withPrefix(messageKeyPrefix)

  private val view: AddAssetsView = viewFor[AddAssetsView](Some(emptyUserAnswers))

  private def applyView(form: Form[_]): HtmlFormat.Appendable =
    view.apply(form, Nil, "Add assets", Nil, index)(fakeRequest, messages)

  private def applyView(form: Form[_], completeAssets: Seq[AddRow], count: Int): HtmlFormat.Appendable = {
    val title = if (count > 1) s"You have added $count assets" else "Add assets"
    view.apply(form, completeAssets, title, Nil, index)(fakeRequest, messages)
  }

  "AddAsset View" when {

    "there are no assets" must {
      behave like normalPage(applyView(form), messageKeyPrefix)

      behave like pageWithNoTabularData(applyView(form))

      behave like pageWithBackLink(applyView(form))

      behave like pageWithOptions(form, applyView, AddAssets.options(messageKeyPrefix))
    }

    "there are assets" must {

      val viewWithData = applyView(form, completeAssets, 2)

      behave like dynamicTitlePage(viewWithData, s"$messageKeyPrefix.count", "2")

      behave like pageWithBackLink(viewWithData)

      behave like pageWithOptions(form, applyView, AddAssets.options(messageKeyPrefix))
    }

  }
}
