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

package controllers.asset.property_or_land

import base.SpecBase
import controllers.routes._
import models.Status.Completed
import models.WhatKindOfAsset.{Other, PropertyOrLand}
import models.{InternationalAddress, NormalMode, UKAddress}
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.CheckYourAnswersHelper
import utils.countryOptions.CountryOptions
import viewmodels.AnswerSection
import views.html.asset.property_or_land.PropertyOrLandAnswersView

class PropertyOrLandAnswerControllerSpec extends SpecBase {

  def onwardRoute = Call("GET", "/foo")

  val index: Int = 0

  lazy val propertyOrLandAnswerRoute: String = routes.PropertyOrLandAnswerController.onPageLoad(index, fakeDraftId).url

  "PropertyOrLandAnswer Controller" must {

    "property or land does not have an address and total value is owned by the trust" must {

      "return OK and the correct view for a GET" in {

        val userAnswers =
          emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), false).success.value
            .set(PropertyOrLandDescriptionPage(index), "Property Land Description").success.value
            .set(PropertyOrLandTotalValuePage(index), "10000").success.value
            .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value
            .set(AssetStatus(index), Completed).success.value

        val countryOptions = injector.instanceOf[CountryOptions]
        val checkYourAnswersHelper = new CheckYourAnswersHelper(countryOptions)(userAnswers, fakeDraftId, canEdit = true)

        val expectedSections = Seq(
          AnswerSection(
            None,
            Seq(
              checkYourAnswersHelper.whatKindOfAsset(index).value,
              checkYourAnswersHelper.propertyOrLandAddressYesNo(index).value,
              checkYourAnswersHelper.propertyOrLandDescription(index).value,
              checkYourAnswersHelper.propertyOrLandTotalValue(index).value,
              checkYourAnswersHelper.trustOwnAllThePropertyOrLand(index).value
            )
          )
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, propertyOrLandAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyOrLandAnswersView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

        application.stop()
      }

    }

    "property or land does not have an address and total value is not owned by the trust" must {

      "return OK and the correct view for a GET" in {

        val userAnswers =
          emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), false).success.value
            .set(PropertyOrLandDescriptionPage(index), "Property Land Description").success.value
            .set(PropertyOrLandTotalValuePage(index), "10000").success.value
            .set(TrustOwnAllThePropertyOrLandPage(index), false).success.value
            .set(PropertyLandValueTrustPage(index), "10").success.value
            .set(AssetStatus(index), Completed).success.value

        val countryOptions = injector.instanceOf[CountryOptions]
        val checkYourAnswersHelper = new CheckYourAnswersHelper(countryOptions)(userAnswers, fakeDraftId, canEdit = true)

        val expectedSections = Seq(
          AnswerSection(
            None,
            Seq(
              checkYourAnswersHelper.whatKindOfAsset(index).value,
              checkYourAnswersHelper.propertyOrLandAddressYesNo(index).value,
              checkYourAnswersHelper.propertyOrLandDescription(index).value,
              checkYourAnswersHelper.propertyOrLandTotalValue(index).value,
              checkYourAnswersHelper.trustOwnAllThePropertyOrLand(index).value,
              checkYourAnswersHelper.propertyLandValueTrust(index).value
            )
          )
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, propertyOrLandAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyOrLandAnswersView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

        application.stop()
      }

    }

    "property or land has a UK address and total value is owned by the trust" must {

      "return OK and the correct view for a GET" in {

        val userAnswers =
          emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), true).success.value
            .set(PropertyOrLandAddressUkYesNoPage(index), true).success.value
            .set(PropertyOrLandUKAddressPage(index), UKAddress("Line1", "Line2", None, None, "NE62RT")).success.value
            .set(PropertyOrLandTotalValuePage(index), "10000").success.value
            .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value
            .set(AssetStatus(index), Completed).success.value

        val countryOptions = injector.instanceOf[CountryOptions]
        val checkYourAnswersHelper = new CheckYourAnswersHelper(countryOptions)(userAnswers, fakeDraftId, canEdit = true)

        val expectedSections = Seq(
          AnswerSection(
            None,
            Seq(
              checkYourAnswersHelper.whatKindOfAsset(index).value,
              checkYourAnswersHelper.propertyOrLandAddressYesNo(index).value,
              checkYourAnswersHelper.propertyOrLandAddressUkYesNo(index).value,
              checkYourAnswersHelper.propertyOrLandUKAddress(index).value,
              checkYourAnswersHelper.propertyOrLandTotalValue(index).value,
              checkYourAnswersHelper.trustOwnAllThePropertyOrLand(index).value
            )
          )
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, propertyOrLandAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyOrLandAnswersView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

        application.stop()
      }

    }

    "property or land has a UK address and total value is not owned by the trust" must {

      "return OK and the correct view for a GET" in {

        val userAnswers =
          emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), true).success.value
            .set(PropertyOrLandAddressUkYesNoPage(index), true).success.value
            .set(PropertyOrLandUKAddressPage(index), UKAddress("Line1", "Line2", None, None, "NE62RT")).success.value
            .set(PropertyOrLandTotalValuePage(index), "10000").success.value
            .set(TrustOwnAllThePropertyOrLandPage(index), false).success.value
            .set(PropertyLandValueTrustPage(index), "10").success.value
            .set(AssetStatus(index), Completed).success.value

        val countryOptions = injector.instanceOf[CountryOptions]
        val checkYourAnswersHelper = new CheckYourAnswersHelper(countryOptions)(userAnswers, fakeDraftId, canEdit = true)

        val expectedSections = Seq(
          AnswerSection(
            None,
            Seq(
              checkYourAnswersHelper.whatKindOfAsset(index).value,
              checkYourAnswersHelper.propertyOrLandAddressYesNo(index).value,
              checkYourAnswersHelper.propertyOrLandAddressUkYesNo(index).value,
              checkYourAnswersHelper.propertyOrLandUKAddress(index).value,
              checkYourAnswersHelper.propertyOrLandTotalValue(index).value,
              checkYourAnswersHelper.trustOwnAllThePropertyOrLand(index).value,
              checkYourAnswersHelper.propertyLandValueTrust(index).value
            )
          )
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, propertyOrLandAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyOrLandAnswersView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

        application.stop()
      }

    }

    "property or land has a International address and total value is owned by the trust" must {

      "return OK and the correct view for a GET" in {

        val userAnswers =
          emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), true).success.value
            .set(PropertyOrLandAddressUkYesNoPage(index), false).success.value
            .set(PropertyOrLandInternationalAddressPage(index), InternationalAddress("line1", "line2", Some("line3"), "ES")).success.value
            .set(PropertyOrLandTotalValuePage(index), "10000").success.value
            .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value
            .set(AssetStatus(index), Completed).success.value

        val countryOptions = injector.instanceOf[CountryOptions]
        val checkYourAnswersHelper = new CheckYourAnswersHelper(countryOptions)(userAnswers, fakeDraftId, canEdit = true)

        val expectedSections = Seq(
          AnswerSection(
            None,
            Seq(
              checkYourAnswersHelper.whatKindOfAsset(index).value,
              checkYourAnswersHelper.propertyOrLandAddressYesNo(index).value,
              checkYourAnswersHelper.propertyOrLandAddressUkYesNo(index).value,
              checkYourAnswersHelper.propertyOrLandInternationalAddress(index).value,
              checkYourAnswersHelper.propertyOrLandTotalValue(index).value,
              checkYourAnswersHelper.trustOwnAllThePropertyOrLand(index).value
            )
          )
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, propertyOrLandAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyOrLandAnswersView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

        application.stop()
      }

    }

    "property or land has a International address and total value is not owned by the trust" must {

      "return OK and the correct view for a GET" in {

        val userAnswers =
          emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), true).success.value
            .set(PropertyOrLandAddressUkYesNoPage(index), false).success.value
            .set(PropertyOrLandInternationalAddressPage(index), InternationalAddress("line1", "line2", Some("line3"), "ES")).success.value
            .set(PropertyOrLandTotalValuePage(index), "10000").success.value
            .set(TrustOwnAllThePropertyOrLandPage(index), false).success.value
            .set(PropertyLandValueTrustPage(index), "10").success.value
            .set(AssetStatus(index), Completed).success.value

        val countryOptions = injector.instanceOf[CountryOptions]
        val checkYourAnswersHelper = new CheckYourAnswersHelper(countryOptions)(userAnswers, fakeDraftId, canEdit = true)

        val expectedSections = Seq(
          AnswerSection(
            None,
            Seq(
              checkYourAnswersHelper.whatKindOfAsset(index).value,
              checkYourAnswersHelper.propertyOrLandAddressYesNo(index).value,
              checkYourAnswersHelper.propertyOrLandAddressUkYesNo(index).value,
              checkYourAnswersHelper.propertyOrLandInternationalAddress(index).value,
              checkYourAnswersHelper.propertyOrLandTotalValue(index).value,
              checkYourAnswersHelper.trustOwnAllThePropertyOrLand(index).value,
              checkYourAnswersHelper.propertyLandValueTrust(index).value
            )
          )
        )

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        val request = FakeRequest(GET, propertyOrLandAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyOrLandAnswersView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, fakeDraftId, expectedSections)(fakeRequest, messages).toString

        application.stop()
      }

    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, propertyOrLandAnswerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad().url

      application.stop()
    }

  }
}
