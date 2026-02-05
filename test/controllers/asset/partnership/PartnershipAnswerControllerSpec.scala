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

package controllers.asset.partnership

import base.SpecBase
import connectors.TrustsConnector
import mapping.PartnershipAssetMapper
import models.WhatKindOfAsset.Partnership
import models.assets._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import pages.asset.WhatKindOfAssetPage
import pages.asset.partnership._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import utils.print.PartnershipPrintHelper
import views.html.asset.partnership.PartnershipAnswersView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class PartnershipAnswerControllerSpec extends SpecBase {

  val validDate: LocalDate = LocalDate.now(ZoneOffset.UTC)
  val name: String         = "Description"

  private val partnershipAnswerRoute = "/maintain-a-trust/trust-assets/partnership/0/partnership-check-answers"

  "PartnershipAnswer Controller" must {

    val answers =
      emptyUserAnswers
        .set(WhatKindOfAssetPage(index), Partnership)
        .success
        .value
        .set(PartnershipDescriptionPage(index), "Partnership Description")
        .success
        .value
        .set(PartnershipStartDatePage(index), validDate)
        .success
        .value

    "on GET" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, partnershipAnswerRoute)

        val result = route(application, request).value

        val view          = application.injector.instanceOf[PartnershipAnswersView]
        val printHelper   = application.injector.instanceOf[PartnershipPrintHelper]
        val answerSection = printHelper(answers, index, provisional = true, name)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, answerSection)(request, messages).toString

        application.stop()
      }

      "redirect to Session Expired if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(GET, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }
    }

    "on POST" must {

      "return INTERNAL_SERVER_ERROR when mapper returns None" in {
        val mockTrustConnector = mock[TrustsConnector]
        val mockMapper         = mock[PartnershipAssetMapper]

        when(mockMapper.apply(any())).thenReturn(None)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[TrustsConnector].toInstance(mockTrustConnector),
            bind[PartnershipAssetMapper].toInstance(mockMapper)
          )
          .build()

        val request = FakeRequest(POST, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        application.stop()
      }

      "take the amend branch and redirect when amending the last existing partnership asset (status OK)" in {
        val mockTrustConnector = mock[TrustsConnector]
        val mockMapper         = mock[PartnershipAssetMapper]

        val mapped = PartnershipType("Partnership Description", validDate)

        when(mockMapper.apply(any())).thenReturn(Some(mapped))
        when(mockTrustConnector.getAssets(any())(any(), any()))
          .thenReturn(
            Future.successful(
              models.assets.Assets(
                Nil,
                Nil,
                Nil,
                Nil,
                List(PartnershipType("Old partnership", validDate.minusDays(2))),
                Nil,
                Nil
              )
            )
          )
        when(mockTrustConnector.amendPartnershipAsset(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[TrustsConnector].toInstance(mockTrustConnector),
            bind[PartnershipAssetMapper].toInstance(mockMapper)
          )
          .build()

        val request = FakeRequest(POST, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController
          .onPageLoad()
          .url

        application.stop()
      }

      "also redirect when amending the last existing partnership asset (status NO_CONTENT)" in {
        val mockTrustConnector = mock[TrustsConnector]
        val mockMapper         = mock[PartnershipAssetMapper]

        val mapped = PartnershipType("Partnership Description", validDate)

        when(mockMapper.apply(any())).thenReturn(Some(mapped))
        when(mockTrustConnector.getAssets(any())(any(), any()))
          .thenReturn(
            Future.successful(
              models.assets.Assets(
                Nil,
                Nil,
                Nil,
                Nil,
                List(PartnershipType("Old partnership", validDate.minusDays(2))),
                Nil,
                Nil
              )
            )
          )
        when(mockTrustConnector.amendPartnershipAsset(any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[TrustsConnector].toInstance(mockTrustConnector),
            bind[PartnershipAssetMapper].toInstance(mockMapper)
          )
          .build()

        val request = FakeRequest(POST, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController
          .onPageLoad()
          .url

        application.stop()
      }

      "take the add branch when asset does not already exist and redirect" in {
        val mockTrustConnector = mock[TrustsConnector]
        val mockMapper         = mock[PartnershipAssetMapper]

        val mapped = PartnershipType("Partnership Description", validDate)

        when(mockMapper.apply(any())).thenReturn(Some(mapped))
        when(mockTrustConnector.getAssets(any())(any(), any()))
          .thenReturn(
            Future.successful(
              models.assets.Assets(
                Nil,
                Nil,
                Nil,
                Nil,
                Nil,
                Nil,
                Nil
              )
            )
          )
        when(mockTrustConnector.addPartnershipAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[TrustsConnector].toInstance(mockTrustConnector),
            bind[PartnershipAssetMapper].toInstance(mockMapper)
          )
          .build()

        val request = FakeRequest(POST, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController
          .onPageLoad()
          .url

        application.stop()
      }

      "take the exists branch (duplicate found) and redirect without adding" in {
        val mockTrustConnector = mock[TrustsConnector]
        val mockMapper         = mock[PartnershipAssetMapper]

        val mapped = PartnershipType("Partnership Description", validDate)

        when(mockMapper.apply(any())).thenReturn(Some(mapped))
        when(mockTrustConnector.getAssets(any())(any(), any()))
          .thenReturn(
            Future.successful(
              models.assets.Assets(
                Nil,
                Nil,
                Nil,
                Nil,
                List(
                  PartnershipType("Partnership Description", validDate),
                  PartnershipType("Another partnership", validDate.minusDays(5))
                ),
                Nil,
                Nil
              )
            )
          )

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[TrustsConnector].toInstance(mockTrustConnector),
            bind[PartnershipAssetMapper].toInstance(mockMapper)
          )
          .build()

        val request = FakeRequest(POST, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController
          .onPageLoad()
          .url

        verify(mockTrustConnector, never()).addPartnershipAsset(any(), any())(any(), any())

        application.stop()
      }

      "redirect to Session Expired if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(POST, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }
    }
  }

}
