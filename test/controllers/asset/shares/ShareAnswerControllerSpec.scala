/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.asset.shares

import base.SpecBase
import models.Status.Completed
import models.WhatKindOfAsset.Shares
import models._
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.shares._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.CheckYourAnswersHelper
import utils.countryOptions.CountryOptions
import viewmodels.AnswerSection
import views.html.asset.shares.ShareAnswersView

class ShareAnswerControllerSpec extends SpecBase {

  val index: Int = 0

  lazy val shareAnswerRoute = routes.ShareAnswerController.onPageLoad(index, fakeDraftId).url

  "ShareAnswer Controller" must {

    "return OK and the correct view for a GET (share)" in {

      val userAnswers =
        emptyUserAnswers
        .set(SharesInAPortfolioPage(index), false).success.value
        .set(ShareCompanyNamePage(index), "Share Company Name").success.value
        .set(SharesOnStockExchangePage(index), true).success.value
        .set(ShareClassPage(index), ShareClass.Ordinary).success.value
        .set(ShareQuantityInTrustPage(index), "1000").success.value
        .set(ShareValueInTrustPage(index), "10").success.value
        .set(AssetStatus(index), Completed).success.value

      val countryOptions = injector.instanceOf[CountryOptions]
      val checkYourAnswersHelper = new CheckYourAnswersHelper(countryOptions)(userAnswers, fakeDraftId, canEdit = true)

      val expectedSections = Seq(
        AnswerSection(
          None,
          Seq(
            checkYourAnswersHelper.sharesInAPortfolio(index).value,
            checkYourAnswersHelper.shareCompanyName(index).value,
            checkYourAnswersHelper.sharesOnStockExchange(index).value,
            checkYourAnswersHelper.shareClass(index).value,
            checkYourAnswersHelper.shareQuantityInTrust(index).value,
            checkYourAnswersHelper.shareValueInTrust(index).value
          )
        )
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, shareAnswerRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ShareAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

      application.stop()
    }

    "return OK and the correct view for a GET (share portfolio)" in {

      val userAnswers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage(index), Shares).success.value
          .set(SharesInAPortfolioPage(index), true).success.value
          .set(SharePortfolioNamePage(index), "Share Portfolio Name").success.value
          .set(SharePortfolioOnStockExchangePage(index), true).success.value
          .set(SharePortfolioQuantityInTrustPage(index), "2000").success.value
          .set(SharePortfolioValueInTrustPage(index), "20").success.value

      val countryOptions = injector.instanceOf[CountryOptions]
      val checkYourAnswersHelper = new CheckYourAnswersHelper(countryOptions)(userAnswers, fakeDraftId, canEdit = true)

      val expectedSections = Seq(
        AnswerSection(
          None,
          Seq(
            checkYourAnswersHelper.whatKindOfAsset(index).value,
            checkYourAnswersHelper.sharesInAPortfolio(index).value,
            checkYourAnswersHelper.sharePortfolioName(index).value,
            checkYourAnswersHelper.sharePortfolioOnStockExchange(index).value,
            checkYourAnswersHelper.sharePortfolioQuantityInTrust(index).value,
            checkYourAnswersHelper.sharePortfolioValueInTrust(index).value
          )
        )
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, shareAnswerRoute)

      val result = route(application, request).value

      val view = application.injector.instanceOf[ShareAnswersView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, shareAnswerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

  }
}
