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

package views.behaviours

import play.api.data.{Form, FormError}
import play.twirl.api.HtmlFormat

trait QuestionViewBehaviours[A] extends ViewBehaviours {

  val errorKey         = "value"
  val errorPrefix      = "site.error"
  val errorMessage     = "error.number"
  val error: FormError = FormError(errorKey, errorMessage)

  val form: Form[A]

  def pageWithDateFields(
    form: Form[A],
    createView: Form[A] => HtmlFormat.Appendable,
    messageKeyPrefix: String,
    key: String,
    args: String*
  ): Unit = {

    val fields = Seq(s"$key.day", s"$key.month", s"$key.year")

    "behave like a question page" when {

      "rendered" must {

        for (field <- fields)

          s"contain an input for $field" in {
            val doc = asDocument(createView(form))
            assertRenderedById(doc, field)
          }

        "not render an error summary" in {

          val doc = asDocument(createView(form))
          assertNotRenderedByClass(doc, "govuk-error-summary")
        }
      }

      "rendered with any error" must {

        "show an error prefix in the browser title" in {

          val doc = asDocument(createView(form.withError(error)))
          assertEqualsValue(
            doc,
            "title",
            mockViewUtils.breadcrumbTitle(
              s"""${messages("error.browser.title.prefix")} ${messages(s"$messageKeyPrefix.title", args: _*)}"""
            )(fakeRequest, messages)
          )
        }
      }

      s"rendered with an error" must {

        "show an error summary" in {

          val doc = asDocument(createView(form.withError(FormError(key, "error"))))
          assertRenderedByClass(doc, "govuk-error-summary")
        }

        s"show an error in the legend" in {

          val doc = asDocument(createView(form.withError(FormError(key, "error"))))
          assertRenderedById(doc, s"$key-error")
        }
      }
    }
  }

}
