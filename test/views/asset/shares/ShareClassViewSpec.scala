/*
 * Copyright 2022 HM Revenue & Customs
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

package views.asset.shares

import forms.shares.ShareClassFormProvider
import models.ShareClass._
import models.{NormalMode, ShareClass}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.asset.shares.ShareClassView

class ShareClassViewSpec extends ViewBehaviours {

  private val messageKeyPrefix: String = "shares.class"
  private val companyName: String = "Company"
  private val form: Form[ShareClass] = new ShareClassFormProvider()()

  private val view: ShareClassView = viewFor[ShareClassView](Some(emptyUserAnswers))

  private def applyView(form: Form[_]): HtmlFormat.Appendable =
    view.apply(form, NormalMode, companyName)(fakeRequest, messages)

  "ShareClass view" must {

    behave like dynamicTitlePage(applyView(form), messageKeyPrefix, companyName)

    behave like pageWithBackLink(applyView(form))

    pageWithASubmitButton(applyView(form))
  }

  "ShareClass view" when {

    "rendered" must {

      "contain radio buttons for the value" in {

        val doc = asDocument(applyView(form))

        for (option <- ShareClass.allOptions) {
          assertContainsRadioButton(doc, option.id, "value", option.value, isChecked = false)
        }
      }

      "not render radio buttons that are for maintain" in {
        val doc = asDocument(applyView(form))

        val excluded = ShareClass.asRadioOptions(List(
          NonVoting, Redeemable, Management, OtherClasses, Voting, Dividend, Capital
        ))

        for (option <- excluded) {
          assertNotRenderedById(doc, option.id)
        }
      }
    }

    for (option <- ShareClass.allOptions) {

      s"rendered with a value of '${option.value}'" must {

        s"have the '${option.value}' radio button selected" in {

          val doc = asDocument(applyView(form.bind(Map("value" -> s"${option.value}"))))

          assertContainsRadioButton(doc, option.id, "value", option.value, isChecked = true)

          for (unselectedOption <- ShareClass.allOptions.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, unselectedOption.id, "value", unselectedOption.value, isChecked = false)
          }
        }
      }
    }
  }
}
