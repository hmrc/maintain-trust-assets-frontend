/*
 * Copyright 2026 HM Revenue & Customs
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

package views

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.OutOfBoundsPageNotFoundView

class OutOfBoundsPageNotFoundViewSpec extends ViewBehaviours {

  private val messageKeyPrefix: String = "outOfBoundsPageNotFound"

  private def applyView(migrating: Boolean): HtmlFormat.Appendable = {
    val ua   = emptyUserAnswers.copy(isMigratingToTaxable = migrating)
    val view = viewFor[OutOfBoundsPageNotFoundView](Some(ua))
    view.apply(migrating)(fakeRequest, messages)
  }

  "OutOfBoundsPageNotFound view" must {

    behave like normalPage(applyView(migrating = true), messageKeyPrefix)
    behave like pageWithBackLink(applyView(migrating = true))

    "link bullet1 to the trust overview" in {
      val links = asDocument(applyView(migrating = true))
        .select("ul.govuk-list--bullet li a.govuk-link")
      links.get(0).text()       mustBe messages(s"$messageKeyPrefix.bullet1")
      links.get(0).attr("href") mustBe frontendAppConfig.maintainATrustOverview
    }

    "link bullet2 to the migrating add-assets page when migrating to taxable" in {
      val link = asDocument(applyView(migrating = true))
        .select("ul.govuk-list--bullet li a.govuk-link")
        .get(1)
      link.text()       mustBe messages(s"$messageKeyPrefix.bullet2")
      link.attr("href") mustBe
        controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url
    }

    "link bullet2 to the non-EEA add-assets page when not migrating" in {
      val link = asDocument(applyView(migrating = false))
        .select("ul.govuk-list--bullet li a.govuk-link")
        .get(1)
      link.attr("href") mustBe
        controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad().url
    }
  }

}
