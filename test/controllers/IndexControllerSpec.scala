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

package controllers

import base.SpecBase
import connectors.SubmissionDraftConnector
import controllers.asset.routes._
import models.Status.Completed
import models.{UserAnswers, WhatKindOfAsset}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.money.AssetMoneyValuePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FeatureFlagService

import scala.concurrent.Future

class IndexControllerSpec extends SpecBase {

  private val featureFlagService: FeatureFlagService = mock[FeatureFlagService]
  private val submissionDraftConnector: SubmissionDraftConnector = mock[SubmissionDraftConnector]

  private lazy val onPageLoadRoute: String = routes.IndexController.onPageLoad(fakeDraftId).url

  "Index Controller" when {

    "pre-existing user answers" must {

      "redirect to AddAssetsController" when {
        "existing assets" in {

          reset(registrationsRepository)

          val userAnswers: UserAnswers = emptyUserAnswers
            .set(WhatKindOfAssetPage(0), WhatKindOfAsset.Money).success.value
            .set(AssetMoneyValuePage(0), 100L).success.value
            .set(AssetStatus(0), Completed).success.value

          val application = applicationBuilder()
            .overrides(
              bind[FeatureFlagService].toInstance(featureFlagService),
              bind[SubmissionDraftConnector].toInstance(submissionDraftConnector)
            ).build()

          when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(userAnswers)))
          when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
          when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))
          when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(false))

          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe AddAssetsController.onPageLoad(fakeDraftId).url

          application.stop()
        }
      }

      "redirect to AssetInterruptPageController" when {
        "no existing assets" in {

          reset(registrationsRepository)

          val application = applicationBuilder()
            .overrides(
              bind[FeatureFlagService].toInstance(featureFlagService),
              bind[SubmissionDraftConnector].toInstance(submissionDraftConnector)
            ).build()

          when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(emptyUserAnswers)))
          when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
          when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))
          when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(false))

          val request = FakeRequest(GET, onPageLoadRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe AssetInterruptPageController.onPageLoad(fakeDraftId).url

          application.stop()
        }
      }

      "update value of is5mldEnabled and isTaxable in user answers" in {

        reset(registrationsRepository)

        val userAnswers = emptyUserAnswers.copy(is5mldEnabled = false)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[FeatureFlagService].toInstance(featureFlagService),
            bind[SubmissionDraftConnector].toInstance(submissionDraftConnector)
          ).build()

        when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(Some(userAnswers)))
        when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
        when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))
        when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(true))

        val request = FakeRequest(GET, onPageLoadRoute)

        route(application, request).value.map { _ =>
          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

          uaCaptor.getValue.is5mldEnabled mustBe true
          uaCaptor.getValue.isTaxable mustBe true

          application.stop()
        }
      }
    }

    "no pre-existing user answers" must {

      "redirect to AssetInterruptPageController" in {

        reset(registrationsRepository)

        val application = applicationBuilder()
          .overrides(
            bind[FeatureFlagService].toInstance(featureFlagService),
            bind[SubmissionDraftConnector].toInstance(submissionDraftConnector)
          ).build()

        when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
        when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
        when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))
        when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(false))

        val request = FakeRequest(GET, onPageLoadRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe AssetInterruptPageController.onPageLoad(fakeDraftId).url

        application.stop()
      }

      "instantiate new set of user answers" when {

        "5mld enabled" when {

          "taxable" must {
            "add is5mldEnabled = true and isTaxable = true to user answers" in {

              reset(registrationsRepository)

              val application = applicationBuilder(userAnswers = None)
                .overrides(
                  bind[FeatureFlagService].toInstance(featureFlagService),
                  bind[SubmissionDraftConnector].toInstance(submissionDraftConnector)
                ).build()

              when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
              when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
              when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))
              when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(true))

              val request = FakeRequest(GET, onPageLoadRoute)

              route(application, request).value.map { _ =>
                val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
                verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

                uaCaptor.getValue.is5mldEnabled mustBe true
                uaCaptor.getValue.isTaxable mustBe true
                uaCaptor.getValue.draftId mustBe fakeDraftId
                uaCaptor.getValue.internalAuthId mustBe "internalId"

                application.stop()
              }
            }
          }

          "non-taxable" must {
            "add is5mldEnabled = true and isTaxable = false to user answers" in {

              reset(registrationsRepository)

              val application = applicationBuilder(userAnswers = None)
                .overrides(
                  bind[FeatureFlagService].toInstance(featureFlagService),
                  bind[SubmissionDraftConnector].toInstance(submissionDraftConnector)
                ).build()

              when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
              when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
              when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(true))
              when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(false))

              val request = FakeRequest(GET, onPageLoadRoute)

              route(application, request).value.map { _ =>
                val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
                verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

                uaCaptor.getValue.is5mldEnabled mustBe true
                uaCaptor.getValue.isTaxable mustBe false
                uaCaptor.getValue.draftId mustBe fakeDraftId
                uaCaptor.getValue.internalAuthId mustBe "internalId"

                application.stop()
              }
            }
          }
        }

        "5mld not enabled" must {
          "add is5mldEnabled = false to user answers" in {

            reset(registrationsRepository)

            val application = applicationBuilder(userAnswers = None)
              .overrides(
                bind[FeatureFlagService].toInstance(featureFlagService),
                bind[SubmissionDraftConnector].toInstance(submissionDraftConnector)
              ).build()

            when(registrationsRepository.get(any())(any())).thenReturn(Future.successful(None))
            when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
            when(featureFlagService.is5mldEnabled()(any(), any())).thenReturn(Future.successful(false))
            when(submissionDraftConnector.getIsTrustTaxable(any())(any(), any())).thenReturn(Future.successful(false))

            val request = FakeRequest(GET, onPageLoadRoute)

            route(application, request).value.map { _ =>
              val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
              verify(registrationsRepository).set(uaCaptor.capture)(any(), any())

              uaCaptor.getValue.is5mldEnabled mustBe false
              uaCaptor.getValue.isTaxable mustBe false
              uaCaptor.getValue.draftId mustBe fakeDraftId
              uaCaptor.getValue.internalAuthId mustBe "internalId"

              application.stop()
            }
          }
        }
      }
    }
  }
}
