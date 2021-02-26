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

  private val fakeAddRow: AddRow = AddRow("name", "label", "change", "remove")

  private def completeRows(number: Int): List[AddRow] = List.fill(number)(fakeAddRow)

  "MaxedOutView" when {

    "taxable" when {

      val messageKeyPrefix: String = "addAssets"

      "4mld" must {

        val max: Int = 51

        val view: MaxedOutView = viewFor[MaxedOutView](Some(emptyUserAnswers.copy(is5mldEnabled = false, isTaxable = true)))

        def applyView(): HtmlFormat.Appendable =
          view.apply(fakeDraftId, Nil, completeRows(max), "Add assets", max, messageKeyPrefix)(fakeRequest, messages)

        behave like normalPage(applyView(), messageKeyPrefix)

        behave like pageWithBackLink(applyView())

        behave like pageWithCompleteTabularData(applyView(), completeRows(max))

        behave like pageWithASubmitButton(applyView())

        "show maxed out assets content" in {
          val doc = asDocument(applyView())

          assertContainsText(doc, s"You cannot add another asset as you have entered a maximum of $max.")
          assertContainsText(doc, "You can add another asset by removing an existing one, or write to HMRC with details of any additional assets.")
        }
      }

      "5mld" must {

        val max: Int = 76

        val view: MaxedOutView = viewFor[MaxedOutView](Some(emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = true)))

        def applyView(): HtmlFormat.Appendable =
          view.apply(fakeDraftId, Nil, completeRows(max), "Add assets", max, messageKeyPrefix)(fakeRequest, messages)

        behave like normalPage(applyView(), messageKeyPrefix)

        behave like pageWithBackLink(applyView())

        behave like pageWithCompleteTabularData(applyView(), completeRows(max))

        behave like pageWithASubmitButton(applyView())

        "show maxed out assets content" in {
          val doc = asDocument(applyView())

          assertContainsText(doc, s"You cannot add another asset as you have entered a maximum of $max.")
          assertContainsText(doc, "You can add another asset by removing an existing one, or write to HMRC with details of any additional assets.")
        }
      }
    }

    "non-taxable" must {

      val messageKeyPrefix: String = "addAssets.nonTaxable"

      val max: Int = 25

      val view: MaxedOutView = viewFor[MaxedOutView](Some(emptyUserAnswers.copy(is5mldEnabled = true, isTaxable = false)))

      def applyView(): HtmlFormat.Appendable =
        view.apply(fakeDraftId, Nil, completeRows(max), "Add a non-EEA company", max, messageKeyPrefix)(fakeRequest, messages)

      behave like normalPage(applyView(), messageKeyPrefix)

      behave like pageWithBackLink(applyView())

      behave like pageWithCompleteTabularData(applyView(), completeRows(max))

      behave like pageWithASubmitButton(applyView())

      "show maxed out assets content" in {
        val doc = asDocument(applyView())

        assertContainsText(doc, s"You cannot add another non-EEA company as you have entered a maximum of $max.")
        assertContainsText(doc, "You can add another non-EEA company by removing an existing one, or write to HMRC with details of any additional non-EEA companies.")
      }
    }
  }
}
