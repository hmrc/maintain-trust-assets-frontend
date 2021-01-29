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

import play.twirl.api.HtmlFormat
import viewmodels.AddRow
import views.behaviours.{OptionsViewBehaviours, TabularDataViewBehaviours}
import views.html.asset.MaxedOutView

class MaxedOutViewSpec extends OptionsViewBehaviours with TabularDataViewBehaviours {

  private val messageKeyPrefix: String = "addAssets"

  private val view: MaxedOutView = viewFor[MaxedOutView](Some(emptyUserAnswers))

  private val fakeAddRow: AddRow = AddRow("name", "label", "change", "remove")

  private val completeRows: List[AddRow] = List.fill(51)(fakeAddRow)

  private def applyView(): HtmlFormat.Appendable =
    view.apply(fakeDraftId, Nil, completeRows, "Add assets", 51)(fakeRequest, messages)

  "MaxedOutView" must {

    val view = applyView()

    behave like normalPage(view, messageKeyPrefix)

    behave like pageWithBackLink(view)

    behave like pageWithCompleteTabularData(view, completeRows)

    behave like pageWithASubmitButton(view)

    "show maxed out assets content" in {
      val doc = asDocument(view)

      assertContainsText(doc, "You cannot add another asset as you have entered a maximum of 51.")
      assertContainsText(doc, "You can add another asset by removing an existing one, or write to HMRC with details of any additional assets.")
    }
  }
}
