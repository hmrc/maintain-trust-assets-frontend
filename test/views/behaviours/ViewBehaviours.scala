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

package views.behaviours

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.ViewSpecBase

trait ViewBehaviours extends ViewSpecBase {

  def normalPage(view: HtmlFormat.Appendable,
                 messageKeyPrefix: String,
                 ignoreTitle : Boolean = false): Unit = {

    "behave like a normal page" when {

      "rendered" must {

        "have the correct banner title" in {

          val doc = asDocument(view)
          val bannerTitle = doc.getElementsByClass("govuk-header__link govuk-header__link--service-name")
          bannerTitle.html() mustBe messages("service.name")
        }

        "display the correct browser title" in {

          val doc = asDocument(view)
          assertEqualsMessage(doc,"title",s"$messageKeyPrefix.title")
        }

        if (!ignoreTitle) {
          pageWithTitle(view, messageKeyPrefix)
        }

        "display language toggles" in {

          val doc = asDocument(view)
          assertRenderedByCssSelector(doc, "a[lang=cy]")
        }
      }
    }
  }

  def pageWithGuidance(view: HtmlFormat.Appendable, messageKeyPrefix: String, expectedGuidanceKeys: String*): Unit = {
    "display the correct guidance" in {

      val doc = asDocument(view)
      for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
    }
  }

  def pageWithTitle(view: HtmlFormat.Appendable, messageKeyPrefix: String, args: Any*) : Unit = {
    "display the correct page title" in {

      val doc = asDocument(view)
      assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix", args: _*)
    }
  }

  def pageWithTitleAndCaption(view: HtmlFormat.Appendable, messageKeyPrefix: String) : Unit = {
    "display the correct page title with section" in {

      val doc = asDocument(view)
      assertPageTitleWithSectionSubheading(doc, messageKeyPrefix)
    }
  }

  def dynamicTitlePage(view: HtmlFormat.Appendable,
                       messageKeyPrefix: String,
                       messageKeyParam: String,
                       expectedGuidanceKeys: String*): Unit = {

    "behave like a dynamic title page" when {

      "rendered" must {

        "have the correct banner title" in {

          val doc = asDocument(view)
          val bannerTitle = doc.getElementsByClass("govuk-header__link govuk-header__link--service-name")
          bannerTitle.html() mustBe messages("service.name")
        }

        "display the correct browser title" in {

          val doc = asDocument(view)
          assertEqualsMessage(doc, "title", s"$messageKeyPrefix.title", messageKeyParam)
        }

        pageWithTitle(view, messageKeyPrefix, messageKeyParam)

        "display the correct guidance" in {

          val doc = asDocument(view)
          for (key <- expectedGuidanceKeys) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
        }

        "display language toggles" in {

          val doc = asDocument(view)
          assertRenderedByCssSelector(doc, "a[lang=cy]")
        }
      }
    }
  }

  def pageWithBackLink(view: HtmlFormat.Appendable): Unit = {

    "behave like a page with a back link" in {
      val doc = asDocument(view)
      assertRenderedById(doc, "back-link")
    }
  }

  def pageWithASubmitButton(view: HtmlFormat.Appendable): Unit = {

    "behave like a page with a submit button" in {
      val doc = asDocument(view)
      assertRenderedById(doc, "submit")
    }
  }

  def pageWithHint[A](form: Form[A],
                      createView: Form[A] => HtmlFormat.Appendable,
                      expectedHintKey: String): Unit = {

    "behave like a page with hint text" in {

      val doc = asDocument(createView(form))
      assertContainsHint(doc, "value", Some(messages(expectedHintKey)))
    }
  }

}
