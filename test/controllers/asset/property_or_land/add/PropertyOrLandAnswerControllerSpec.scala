/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.asset.property_or_land.add

import base.SpecBase
import connectors.TrustsConnector
import controllers.routes._
import models.WhatKindOfAsset.PropertyOrLand
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import pages.asset.WhatKindOfAssetPage
import pages.asset.property_or_land._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.PropertyOrLandPrintHelper
import views.html.asset.property_or_land.add.PropertyOrLandAnswersView

import scala.concurrent.Future

class PropertyOrLandAnswerControllerSpec extends SpecBase {

  private val totalValue: Long = 10000L
  val name: String = "Description"

  lazy val propertyOrLandAnswerRoute: String = routes.PropertyOrLandAnswerController.onPageLoad(index).url

  "PropertyOrLandAnswer Controller" must {

    val answers =
      emptyUserAnswers
        .set(WhatKindOfAssetPage(index), PropertyOrLand).success.value
        .set(PropertyOrLandAddressYesNoPage(index), false).success.value
        .set(PropertyOrLandDescriptionPage(index), "Property Land Description").success.value
        .set(PropertyOrLandTotalValuePage(index), totalValue).success.value
        .set(TrustOwnAllThePropertyOrLandPage(index), true).success.value


    "property or land does not have an address and total value is owned by the trust" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, propertyOrLandAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PropertyOrLandAnswersView]
        val printHelper = application.injector.instanceOf[PropertyOrLandPrintHelper]
        val answerSection = printHelper(answers, index, provisional = true, name)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, answerSection)(request, messages).toString

        application.stop()
      }

    }

    "redirect to the next page when valid data is submitted" in {
      val mockTrustConnector = mock[TrustsConnector]

      val application = applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[TrustsConnector].toInstance(mockTrustConnector))
        .build()
      when(mockTrustConnector.amendPropertyOrLandAsset(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))
      when(mockTrustConnector.addPropertyOrLandAsset( any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(OK, "")))

      val request = FakeRequest(POST, routes.PropertyOrLandAnswerController.onSubmit(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, propertyOrLandAnswerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual SessionExpiredController.onPageLoad.url

      application.stop()
    }

  }
}
