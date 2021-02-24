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

package controllers.asset

import base.SpecBase
import forms.YesNoFormProvider
import models.ShareClass.Ordinary
import models.WhatKindOfAsset._
import models.{InternationalAddress, UKAddress, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito._
import pages.asset._
import pages.asset.business._
import pages.asset.money._
import pages.asset.noneeabusiness._
import pages.asset.other._
import pages.asset.partnership._
import pages.asset.property_or_land._
import pages.asset.shares._
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.asset.RemoveAssetYesNoView

import java.time.LocalDate

class RemoveAssetYesNoControllerSpec extends SpecBase {

  private val formProvider = new YesNoFormProvider()
  private val prefix: String = "assets.removeYesNo"
  private val form: Form[Boolean] = formProvider.withPrefix(prefix)
  private val index: Int = 0
  private val assetValue: Long = 4000L
  private val ukAddress: UKAddress = UKAddress("Line 1", "Line 2", None, None, "POSTCODE")
  private val nonUkAddress: InternationalAddress = InternationalAddress("Line 1", "Line 2", Some("Line 3"), "FR")
  private val date: LocalDate = LocalDate.parse("2000-02-03")

  lazy val removeAssetYesNoRoute: String = routes.RemoveAssetYesNoController.onPageLoad(index, fakeDraftId).url

  "RemoveAssetYesNoController" must {

    "return OK and the correct view for a GET" when {

      "Money asset" when {

        "complete" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), Money).success.value
            .set(AssetMoneyValuePage(index), assetValue).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, removeAssetYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemoveAssetYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, fakeDraftId, index, "£4000")(fakeRequest, messages).toString

          application.stop()
        }

        "in progress" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), Money).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, removeAssetYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemoveAssetYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, fakeDraftId, index, "the asset")(fakeRequest, messages).toString

          application.stop()
        }
      }

      "Property or land asset" when {

        "complete" when {

          "address known" in {

            val userAnswers = emptyUserAnswers
              .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
              .set(PropertyOrLandAddressYesNoPage(index), true).success.value
              .set(PropertyOrLandAddressUkYesNoPage(index), true).success.value
              .set(PropertyOrLandUKAddressPage(index), ukAddress).success.value
              .set(PropertyOrLandTotalValuePage(index), assetValue).success.value
              .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

            val request = FakeRequest(GET, removeAssetYesNoRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RemoveAssetYesNoView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(form, fakeDraftId, index, "Line 1")(fakeRequest, messages).toString

            application.stop()
          }

          "address not known and description known" in {

            val userAnswers = emptyUserAnswers
              .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
              .set(PropertyOrLandAddressYesNoPage(index), false).success.value
              .set(PropertyOrLandDescriptionPage(index), "Property or land description").success.value
              .set(PropertyOrLandTotalValuePage(index), assetValue).success.value
              .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

            val request = FakeRequest(GET, removeAssetYesNoRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RemoveAssetYesNoView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(form, fakeDraftId, index, "Property or land description")(fakeRequest, messages).toString

            application.stop()
          }
        }

        "in progress" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), false).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, removeAssetYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemoveAssetYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, fakeDraftId, index, "the asset")(fakeRequest, messages).toString

          application.stop()
        }
      }

      "Shares asset" when {

        "complete" when {

          "portfolio name" in {

            val userAnswers = emptyUserAnswers
              .set(WhatKindOfAssetPage(index), Shares).success.value
              .set(SharesInAPortfolioPage(index), true).success.value
              .set(SharePortfolioNamePage(index), "Portfolio Name").success.value
              .set(SharePortfolioOnStockExchangePage(index), true).success.value
              .set(SharePortfolioQuantityInTrustPage(index), 100L).success.value
              .set(SharePortfolioValueInTrustPage(index), assetValue).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

            val request = FakeRequest(GET, removeAssetYesNoRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RemoveAssetYesNoView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(form, fakeDraftId, index, "Portfolio Name")(fakeRequest, messages).toString

            application.stop()
          }

          "company name" in {

            val userAnswers = emptyUserAnswers
              .set(WhatKindOfAssetPage(index), Shares).success.value
              .set(SharesInAPortfolioPage(index), false).success.value
              .set(ShareCompanyNamePage(index), "Company Name").success.value
              .set(SharesOnStockExchangePage(index), true).success.value
              .set(ShareClassPage(index), Ordinary).success.value
              .set(ShareQuantityInTrustPage(index), 100L).success.value
              .set(ShareValueInTrustPage(index), assetValue).success.value

            val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

            val request = FakeRequest(GET, removeAssetYesNoRoute)

            val result = route(application, request).value

            val view = application.injector.instanceOf[RemoveAssetYesNoView]

            status(result) mustEqual OK

            contentAsString(result) mustEqual
              view(form, fakeDraftId, index, "Company Name")(fakeRequest, messages).toString

            application.stop()
          }
        }

        "in progress" in {

          val userAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(index), Shares).success.value
            .set(SharesInAPortfolioPage(index), false).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(GET, removeAssetYesNoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RemoveAssetYesNoView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(form, fakeDraftId, index, "the asset")(fakeRequest, messages).toString

          application.stop()
        }
      }

      "Business asset" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(index), Business).success.value
          .set(BusinessNamePage(index), "Business name").success.value
          .set(BusinessDescriptionPage(index), "Business description").success.value
          .set(BusinessAddressUkYesNoPage(index), true).success.value
          .set(BusinessUkAddressPage(index), ukAddress).success.value
          .set(BusinessValuePage(index), assetValue).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, removeAssetYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveAssetYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, fakeDraftId, index, "Business name")(fakeRequest, messages).toString

        application.stop()
      }

      "Partnership asset" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(index), Partnership).success.value
          .set(PartnershipDescriptionPage(index), "Partnership description").success.value
          .set(PartnershipStartDatePage(index), date).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, removeAssetYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveAssetYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, fakeDraftId, index, "Partnership description")(fakeRequest, messages).toString

        application.stop()
      }

      "Other asset" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(index), Other).success.value
          .set(OtherAssetDescriptionPage(index), "Other description").success.value
          .set(OtherAssetValuePage(index), assetValue).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, removeAssetYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveAssetYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, fakeDraftId, index, "Other description")(fakeRequest, messages).toString

        application.stop()
      }

      "Non-EEA business asset" in {

        val userAnswers = emptyUserAnswers
          .set(WhatKindOfAssetPage(index), NonEeaBusiness).success.value
          .set(NamePage(index), "Non-EEA business name").success.value
          .set(InternationalAddressPage(index), nonUkAddress).success.value
          .set(GoverningCountryPage(index), "GB").success.value
          .set(StartDatePage(index), date).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, removeAssetYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveAssetYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, fakeDraftId, index, "Non-EEA business name")(fakeRequest, messages).toString

        application.stop()
      }
    }

    "redirect to add assets page when NO is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, removeAssetYesNoRoute)
          .withFormUrlEncodedBody(("value", "false"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.routes.AddAssetsController.onPageLoad(fakeDraftId).url

      application.stop()
    }

    "remove asset and redirect to add assets page when YES is submitted" in {

      val userAnswers = emptyUserAnswers
        .set(WhatKindOfAssetPage(index), Money).success.value
        .set(AssetMoneyValuePage(index), assetValue).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request =
        FakeRequest(POST, removeAssetYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.routes.AddAssetsController.onPageLoad(fakeDraftId).url

      val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(registrationsRepository).set(uaCaptor.capture)(any(), any())
      uaCaptor.getValue.get(WhatKindOfAssetPage(index)) mustNot be(defined)
      uaCaptor.getValue.get(AssetMoneyValuePage(index)) mustNot be(defined)

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, removeAssetYesNoRoute)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[RemoveAssetYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, fakeDraftId, index, "the asset")(fakeRequest, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, removeAssetYesNoRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, removeAssetYesNoRoute)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
