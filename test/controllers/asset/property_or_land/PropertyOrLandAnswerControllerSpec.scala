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
import models.WhatKindOfAsset.PropertyOrLand
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.print.PropertyOrLandPrintHelper
import views.html.asset.property_or_land.PropertyOrLandAnswersView

class PropertyOrLandAnswerControllerSpec extends SpecBase {

  val index: Int = 0

  private val totalValue: Long = 10000L

  lazy val propertyOrLandAnswerRoute: String = routes.PropertyOrLandAnswerController.onPageLoad(index, fakeDraftId).url

  "PropertyOrLandAnswer Controller" must {

    "property or land does not have an address and total value is owned by the trust" must {

      "return OK and the correct view for a GET" in {

        val userAnswers =
          emptyUserAnswers
            .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
            .set(PropertyOrLandAddressYesNoPage(index), false).success.value
            .set(PropertyOrLandDescriptionPage(index), "Property Land Description").success.value
            .set(PropertyOrLandTotalValuePage(index), totalValue).success.value
            .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value
            .set(AssetStatus(index), Completed).success.value

        val expectedSections = Nil
        val mockPrintHelper: PropertyOrLandPrintHelper = mock[PropertyOrLandPrintHelper]
        when(mockPrintHelper.checkDetailsSection(any(), any(), any(), any())(any())).thenReturn(Nil)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[PropertyOrLandPrintHelper].toInstance(mockPrintHelper))
          .build()

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
