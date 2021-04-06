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

package controllers.asset.noneeabusiness.remove

import base.SpecBase

class RemoveAssetYesNoControllerSpec extends SpecBase {

//  private val formProvider = new YesNoFormProvider()
//  private val prefix: String = "assets.removeYesNo"
//  private val form: Form[Boolean] = formProvider.withPrefix(prefix)
//  private val index: Int = 0
//  private val assetValue: Long = 4000L
//  private val ukAddress: UKAddress = UKAddress("Line 1", "Line 2", None, None, "POSTCODE")
//  private val nonUkAddress: InternationalAddress = InternationalAddress("Line 1", "Line 2", Some("Line 3"), "FR")
//  private val date: LocalDate = LocalDate.parse("2000-02-03")
//
//  lazy val removeAssetYesNoRoute: String = routes.RemoveAssetYesNoController.onPageLoad(index).url
//
//  "RemoveAssetYesNoController" must {
//
//    "return OK and the correct view for a GET" when {
//
//      val taxablePrefix = "assets"
//      val nonTaxablePrefix = "assets.nonTaxable"
//
//      "Money asset" when {
//
//        "complete" in {
//
//          val userAnswers = emptyUserAnswers
//            .set(WhatKindOfAssetPage, Money).success.value
//            .set(AssetMoneyValuePage, assetValue).success.value
//
//          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//          val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//          val result = route(application, request).value
//
//          val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//          status(result) mustEqual OK
//
//          contentAsString(result) mustEqual
//            view(form, index, taxablePrefix, "£4000")(request, messages).toString
//
//          application.stop()
//        }
//
//        "in progress" in {
//
//          val userAnswers = emptyUserAnswers
//            .set(WhatKindOfAssetPage, Money).success.value
//
//          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//          val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//          val result = route(application, request).value
//
//          val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//          status(result) mustEqual OK
//
//          contentAsString(result) mustEqual
//            view(form, index, taxablePrefix, "the asset")(request, messages).toString
//
//          application.stop()
//        }
//      }
//
//      "Property or land asset" when {
//
//        "complete" when {
//
//          "address known" in {
//
//            val userAnswers = emptyUserAnswers
//              .set(WhatKindOfAssetPage, PropertyOrLand).success.value
//              .set(PropertyOrLandAddressYesNoPage, true).success.value
//              .set(PropertyOrLandAddressUkYesNoPage, true).success.value
//              .set(PropertyOrLandUKAddressPage, ukAddress).success.value
//              .set(PropertyOrLandTotalValuePage, assetValue).success.value
//              .set(TrustOwnAllThePropertyOrLandPage, true).success.value
//
//            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//            val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//            val result = route(application, request).value
//
//            val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//            status(result) mustEqual OK
//
//            contentAsString(result) mustEqual
//              view(form, index, taxablePrefix, "Line 1")(request, messages).toString
//
//            application.stop()
//          }
//
//          "address not known and description known" in {
//
//            val userAnswers = emptyUserAnswers
//              .set(WhatKindOfAssetPage, PropertyOrLand).success.value
//              .set(PropertyOrLandAddressYesNoPage, false).success.value
//              .set(PropertyOrLandDescriptionPage, "Property or land description").success.value
//              .set(PropertyOrLandTotalValuePage, assetValue).success.value
//              .set(TrustOwnAllThePropertyOrLandPage, true).success.value
//
//            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//            val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//            val result = route(application, request).value
//
//            val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//            status(result) mustEqual OK
//
//            contentAsString(result) mustEqual
//              view(form, index, taxablePrefix, "Property or land description")(request, messages).toString
//
//            application.stop()
//          }
//        }
//
//        "in progress" in {
//
//          val userAnswers = emptyUserAnswers
//            .set(WhatKindOfAssetPage, PropertyOrLand).success.value
//            .set(PropertyOrLandAddressYesNoPage, false).success.value
//
//          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//          val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//          val result = route(application, request).value
//
//          val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//          status(result) mustEqual OK
//
//          contentAsString(result) mustEqual
//            view(form, index, taxablePrefix, "the asset")(request, messages).toString
//
//          application.stop()
//        }
//      }
//
//      "Shares asset" when {
//
//        "complete" when {
//
//          "portfolio name" in {
//
//            val userAnswers = emptyUserAnswers
//              .set(WhatKindOfAssetPage, Shares).success.value
//              .set(SharesInAPortfolioPage, true).success.value
//              .set(SharePortfolioNamePage, "Portfolio Name").success.value
//              .set(SharePortfolioOnStockExchangePage, true).success.value
//              .set(SharePortfolioQuantityInTrustPage, 100L).success.value
//              .set(SharePortfolioValueInTrustPage, assetValue).success.value
//
//            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//            val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//            val result = route(application, request).value
//
//            val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//            status(result) mustEqual OK
//
//            contentAsString(result) mustEqual
//              view(form, index, taxablePrefix, "Portfolio Name")(request, messages).toString
//
//            application.stop()
//          }
//
//          "company name" in {
//
//            val userAnswers = emptyUserAnswers
//              .set(WhatKindOfAssetPage, Shares).success.value
//              .set(SharesInAPortfolioPage, false).success.value
//              .set(ShareCompanyNamePage, "Company Name").success.value
//              .set(SharesOnStockExchangePage, true).success.value
//              .set(ShareClassPage, Ordinary).success.value
//              .set(ShareQuantityInTrustPage, 100L).success.value
//              .set(ShareValueInTrustPage, assetValue).success.value
//
//            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//            val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//            val result = route(application, request).value
//
//            val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//            status(result) mustEqual OK
//
//            contentAsString(result) mustEqual
//              view(form, index, taxablePrefix, "Company Name")(request, messages).toString
//
//            application.stop()
//          }
//        }
//
//        "in progress" in {
//
//          val userAnswers = emptyUserAnswers
//            .set(WhatKindOfAssetPage, Shares).success.value
//            .set(SharesInAPortfolioPage, false).success.value
//
//          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//          val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//          val result = route(application, request).value
//
//          val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//          status(result) mustEqual OK
//
//          contentAsString(result) mustEqual
//            view(form, index, taxablePrefix, "the asset")(request, messages).toString
//
//          application.stop()
//        }
//      }
//
//      "Business asset" in {
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, Business).success.value
//          .set(BusinessNamePage, "Business name").success.value
//          .set(BusinessDescriptionPage, "Business description").success.value
//          .set(BusinessAddressUkYesNoPage, true).success.value
//          .set(BusinessUkAddressPage, ukAddress).success.value
//          .set(BusinessValuePage, assetValue).success.value
//
//        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//        val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//        status(result) mustEqual OK
//
//        contentAsString(result) mustEqual
//          view(form, index, taxablePrefix, "Business name")(request, messages).toString
//
//        application.stop()
//      }
//
//      "Partnership asset" in {
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, Partnership).success.value
//          .set(PartnershipDescriptionPage, "Partnership description").success.value
//          .set(PartnershipStartDatePage, date).success.value
//
//        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//        val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//        status(result) mustEqual OK
//
//        contentAsString(result) mustEqual
//          view(form, index, taxablePrefix, "Partnership description")(request, messages).toString
//
//        application.stop()
//      }
//
//      "Other asset" in {
//
//        val userAnswers = emptyUserAnswers
//          .set(WhatKindOfAssetPage, Other).success.value
//          .set(OtherAssetDescriptionPage, "Other description").success.value
//          .set(OtherAssetValuePage, assetValue).success.value
//
//        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//        val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//        val result = route(application, request).value
//
//        val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//        status(result) mustEqual OK
//
//        contentAsString(result) mustEqual
//          view(form, index, taxablePrefix, "Other description")(request, messages).toString
//
//        application.stop()
//      }
//
//      "Non-EEA business asset" when {
//
//        "complete" in {
//
//          val userAnswers = emptyUserAnswers
//            .set(WhatKindOfAssetPage, NonEeaBusiness).success.value
//            .set(NamePage, "Non-EEA business name").success.value
//            .set(InternationalAddressPage, nonUkAddress).success.value
//            .set(GoverningCountryPage, "GB").success.value
//            .set(StartDatePage, date).success.value
//
//          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//          val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//          val result = route(application, request).value
//
//          val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//          status(result) mustEqual OK
//
//          contentAsString(result) mustEqual
//            view(form, index, taxablePrefix, "Non-EEA business name")(request, messages).toString
//
//          application.stop()
//        }
//
//        "in progress" when {
//
//          "taxable" in {
//
//            val userAnswers = emptyUserAnswers.copy(isTaxable = true)
//              .set(WhatKindOfAssetPage, NonEeaBusiness).success.value
//
//            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//            val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//            val result = route(application, request).value
//
//            val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//            status(result) mustEqual OK
//
//            contentAsString(result) mustEqual
//              view(form, index, taxablePrefix, "the asset")(request, messages).toString
//
//            application.stop()
//          }
//
//          "non-taxable" in {
//
//            val userAnswers = emptyUserAnswers.copy(isTaxable = false)
//              .set(WhatKindOfAssetPage, NonEeaBusiness).success.value
//
//            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//            val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//            val result = route(application, request).value
//
//            val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//            status(result) mustEqual OK
//
//            contentAsString(result) mustEqual
//              view(form, index, nonTaxablePrefix, "this non-EEA company")(request, messages).toString
//
//            application.stop()
//          }
//        }
//      }
//    }
//
//    "redirect to add assets page when NO is submitted" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      val request =
//        FakeRequest(POST, removeAssetYesNoRoute)
//          .withFormUrlEncodedBody(("value", "false"))
//
//      val result = route(application, request).value
//
//      status(result) mustEqual SEE_OTHER
//
//      redirectLocation(result).value mustEqual controllers.asset.routes.AddAssetsController.onPageLoad().url
//
//      application.stop()
//    }
//
//    "remove asset and redirect to add assets page when YES is submitted" in {
//
//      val userAnswers = emptyUserAnswers
//        .set(WhatKindOfAssetPage, Money).success.value
//        .set(AssetMoneyValuePage, assetValue).success.value
//
//      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//      val request =
//        FakeRequest(POST, removeAssetYesNoRoute)
//          .withFormUrlEncodedBody(("value", "true"))
//
//      val result = route(application, request).value
//
//      status(result) mustEqual SEE_OTHER
//
//      redirectLocation(result).value mustEqual controllers.asset.routes.AddAssetsController.onPageLoad().url
//
//      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
//      verify(playbackRepository).set(uaCaptor.capture)
//      uaCaptor.getValue.get(WhatKindOfAssetPage) mustNot be(defined)
//      uaCaptor.getValue.get(AssetMoneyValuePage) mustNot be(defined)
//
//      application.stop()
//    }
//
//    "return a Bad Request and errors when invalid data is submitted" in {
//
//      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//      val request =
//        FakeRequest(POST, removeAssetYesNoRoute)
//          .withFormUrlEncodedBody(("value", ""))
//
//      val boundForm = form.bind(Map("value" -> ""))
//
//      val view = application.injector.instanceOf[RemoveAssetYesNoView]
//
//      val result = route(application, request).value
//
//      status(result) mustEqual BAD_REQUEST
//
//      contentAsString(result) mustEqual
//        view(boundForm, index, "assets", "the asset")(request, messages).toString
//
//      application.stop()
//    }
//
//    "redirect to Session Expired for a GET if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      val request = FakeRequest(GET, removeAssetYesNoRoute)
//
//      val result = route(application, request).value
//
//      status(result) mustEqual SEE_OTHER
//
//      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
//
//      application.stop()
//    }
//
//    "redirect to Session Expired for a POST if no existing data is found" in {
//
//      val application = applicationBuilder(userAnswers = None).build()
//
//      val request =
//        FakeRequest(POST, removeAssetYesNoRoute)
//          .withFormUrlEncodedBody(("value", "true"))
//
//      val result = route(application, request).value
//
//      status(result) mustEqual SEE_OTHER
//
//      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
//
//      application.stop()
//    }
//  }

}
