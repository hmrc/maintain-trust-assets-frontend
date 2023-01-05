/*
 * Copyright 2023 HM Revenue & Customs
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

package views.asset.business.add

import play.twirl.api.HtmlFormat
import viewmodels.AnswerSection
import views.behaviours.ViewBehaviours
import views.html.asset.business.add.BusinessAnswersView

class BusinessAnswersViewSpec extends ViewBehaviours {

  private val prefix: String = "business.answers"

  private val view: BusinessAnswersView = viewFor[BusinessAnswersView](Some(emptyUserAnswers))

  "BusinessAnswers view" must {

    def applyView(): HtmlFormat.Appendable =
      view.apply(AnswerSection(None, Seq()))(fakeRequest, messages)

    behave like normalPage(applyView(), prefix)

    behave like pageWithBackLink(applyView())
  }
}
