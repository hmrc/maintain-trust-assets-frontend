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
    AddRow("Company 1", WhatKindOfAsset.NonEeaBusiness.toString, "#", "#"),
    AddRow("Company 2", WhatKindOfAsset.NonEeaBusiness.toString, "#", "#")
  )

  private val messageKeyPrefix: String = "addNonEeaBusinessAsset"

  private val form: Form[AddAssets] = new AddAssetsFormProvider().withPrefix(messageKeyPrefix)

  private val view: AddNonEeaBusinessAssetView = viewFor[AddNonEeaBusinessAssetView](Some(emptyUserAnswers))

  private def applyView(form: Form[_]): HtmlFormat.Appendable =
    view.apply(form, Nil, "Add a non-EEA company")(fakeRequest, messages)

  private def applyView(form: Form[_], completeAssets: Seq[AddRow]) = {
    val title = "Add ownership or controlling interest of a company registered outside UK and EEA"
    view.apply(form, completeAssets, title)(fakeRequest, messages)
  }

  "AddNonEeaBusinessAsset View" when {

    "there are no assets" must {
      behave like normalPage(applyView(form), messageKeyPrefix)

      behave like pageWithNoTabularData(applyView(form))

      behave like pageWithBackLink(applyView(form))

      behave like pageWithOptions(form, applyView, AddAssets.options(messageKeyPrefix))
    }

    "there is one asset" must {

      val viewWithData = applyView(form, completeAssets.tail)

      behave like pageWithTitle(viewWithData, s"$messageKeyPrefix")
      behave like pageWithSubTitle(viewWithData, s"$messageKeyPrefix.count.one.subHeading")

      behave like pageWithBackLink(viewWithData)

      behave like pageWithCompleteTabularData(viewWithData, completeAssets.tail)

      behave like pageWithOptions(form, applyView, AddAssets.options(messageKeyPrefix))
    }

    "there are multiple assets" must {

      val viewWithData = applyView(form, completeAssets)

      behave like pageWithTitle(viewWithData, s"$messageKeyPrefix")
      behave like pageWithSubTitle(viewWithData, s"$messageKeyPrefix.count.subHeading", "2")

      behave like pageWithBackLink(viewWithData)

      behave like pageWithCompleteTabularData(viewWithData, completeAssets)

      behave like pageWithOptions(form, applyView, AddAssets.options(messageKeyPrefix))
    }

  }

}
