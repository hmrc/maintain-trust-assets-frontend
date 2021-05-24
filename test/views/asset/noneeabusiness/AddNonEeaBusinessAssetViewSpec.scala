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

package views.asset.noneeabusiness

import forms.AddAssetsFormProvider
import models.{AddAssets, WhatKindOfAsset}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.AddRow
import views.behaviours.{OptionsViewBehaviours, TabularDataViewBehaviours}
import views.html.asset.noneeabusiness.AddNonEeaBusinessAssetView

class AddNonEeaBusinessAssetViewSpec extends OptionsViewBehaviours with TabularDataViewBehaviours {

  private val completeAssets: Seq[AddRow] = Seq(
    AddRow("4500", WhatKindOfAsset.Money.toString, "#", "#"),
    AddRow("4500", WhatKindOfAsset.Money.toString, "#", "#")
  )

  private val messageKeyPrefix: String = "addNonEeaBusinessAsset"

  private val form: Form[AddAssets] = new AddAssetsFormProvider().withPrefix(messageKeyPrefix)

  private val view: AddNonEeaBusinessAssetView = viewFor[AddNonEeaBusinessAssetView](Some(emptyUserAnswers))

  private def applyView(form: Form[_]): HtmlFormat.Appendable =
    view.apply(form, Nil, "Add a non-EEA company")(fakeRequest, messages)

  private def applyView(form: Form[_], inProgressAssets: Seq[AddRow], completeAssets: Seq[AddRow], count: Int): HtmlFormat.Appendable = {
    val title = if (count > 1) s"You have added $count non-EEA companies" else "Add a non-EEA company"
    view.apply(form, completeAssets, title)(fakeRequest, messages)
  }

  "AddNonEeaBusinessAsset View" when {

    "there are no assets" must {
      behave like normalPage(applyView(form), messageKeyPrefix)

      behave like pageWithNoTabularData(applyView(form))

      behave like pageWithBackLink(applyView(form))

      behave like pageWithOptions(form, applyView, AddAssets.options(messageKeyPrefix).toSet)
    }

    "there are assets" must {

      val viewWithData = applyView(form, Nil, completeAssets, 2)

      behave like dynamicTitlePage(viewWithData, s"$messageKeyPrefix.count", "2")

      behave like pageWithBackLink(viewWithData)

      behave like pageWithCompleteTabularData(viewWithData, completeAssets)

      behave like pageWithOptions(form, applyView, AddAssets.options(messageKeyPrefix).toSet)
    }

  }
}
