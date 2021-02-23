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
import forms.{AddAssetsFormProvider, YesNoFormProvider}
import models.AddAssets.NoComplete
import models.Status.Completed
import models.WhatKindOfAsset.{Money, Other, Shares}
import models.{AddAssets, ShareClass, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import pages.AssetStatus
import pages.asset.money._
import pages.asset.other.OtherAssetDescriptionPage
import pages.asset.shares._
import pages.asset.{AddAssetsPage, WhatKindOfAssetPage}
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.AddRow
import views.html.asset.{AddAnAssetYesNoView, AddAssetsView, MaxedOutView}

import scala.concurrent.Future

class AddAssetsControllerSpec extends SpecBase {

  lazy val addAssetsRoute: String = routes.AddAssetsController.onPageLoad(fakeDraftId).url
  lazy val addOnePostRoute: String = routes.AddAssetsController.submitOne(fakeDraftId).url
  lazy val addAnotherPostRoute: String = routes.AddAssetsController.submitAnother(fakeDraftId).url
  lazy val completePostRoute: String = routes.AddAssetsController.submitComplete(fakeDraftId).url

  def changeMoneyAssetRoute(index: Int): String =
    money.routes.AssetMoneyValueController.onPageLoad(index, fakeDraftId).url

  def changeSharesAssetRoute(index: Int): String =
    shares.routes.ShareAnswerController.onPageLoad(index, fakeDraftId).url

  def changeOtherAssetRoute(index: Int): String =
    other.routes.OtherAssetAnswersController.onPageLoad(index, fakeDraftId).url

  def removeAssetYesNoRoute(index: Int): String =
    routes.RemoveAssetYesNoController.onPageLoad(index, fakeDraftId).url

  val addAssetsForm: Form[AddAssets] = new AddAssetsFormProvider()()
  val yesNoForm: Form[Boolean] = new YesNoFormProvider().withPrefix("addAnAssetYesNo")

  lazy val assets = List(
    AddRow("£4800", typeLabel = "Money", changeMoneyAssetRoute(0), removeAssetYesNoRoute(0)),
    AddRow("Share Company Name", typeLabel = "Shares", changeSharesAssetRoute(1), removeAssetYesNoRoute(1))
  )

  val userAnswersWithAssetsComplete: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage(0), Money).success.value
    .set(AssetMoneyValuePage(0), 4800L).success.value
    .set(AssetStatus(0), Completed).success.value
    .set(WhatKindOfAssetPage(1), Shares).success.value
    .set(SharesInAPortfolioPage(1), false).success.value
    .set(ShareCompanyNamePage(1), "Share Company Name").success.value
    .set(SharesOnStockExchangePage(1), true).success.value
    .set(ShareClassPage(1), ShareClass.Ordinary).success.value
    .set(ShareQuantityInTrustPage(1), 1000L).success.value
    .set(ShareValueInTrustPage(1), 10L).success.value
    .set(AssetStatus(1), Completed).success.value

  "AddAssets Controller" when {

    "no data" must {
      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        val request =
          FakeRequest(POST, addAssetsRoute)
            .withFormUrlEncodedBody(("value", AddAssets.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }
    }

    "there are no assets" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAnAssetYesNoView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(addAssetsForm, fakeDraftId)(fakeRequest, messages).toString

        application.stop()
      }

      "redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request =
          FakeRequest(POST, addOnePostRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        val request =
          FakeRequest(POST, addOnePostRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = yesNoForm.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddAnAssetYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, fakeDraftId)(fakeRequest, messages).toString

        application.stop()
      }
    }

    "there are existing assets" must {

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithAssetsComplete)).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAssetsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(addAssetsForm, fakeDraftId, Nil, assets, "You have added 2 assets")(fakeRequest, messages).toString

        application.stop()
      }

      "redirect to the next page when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithAssetsComplete)).build()

        val request =
          FakeRequest(POST, addAnotherPostRoute)
            .withFormUrlEncodedBody(("value", AddAssets.options.head.value))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithAssetsComplete)).build()

        val request =
          FakeRequest(POST, addAnotherPostRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = addAssetsForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddAssetsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, fakeDraftId, Nil, assets, "You have added 2 assets")(fakeRequest, messages).toString

        application.stop()
      }
    }

    "assets maxed out" when {

      val description: String = "Description"

      def userAnswers(max: Int, is5mldEnabled: Boolean, isTaxable: Boolean): UserAnswers = {
        0.until(max).foldLeft(emptyUserAnswers.copy(is5mldEnabled = is5mldEnabled, isTaxable = isTaxable))((ua, i) => {
          ua
            .set(WhatKindOfAssetPage(i), Other).success.value
            .set(OtherAssetDescriptionPage(i), description).success.value
            .set(AssetStatus(i), Completed).success.value
        })
      }

      def assets(max: Int): List[AddRow] = 0.until(max).foldLeft[List[AddRow]](List())((acc, i) => {
        acc :+ AddRow(description, typeLabel = "Other", changeOtherAssetRoute(i), removeAssetYesNoRoute(i))
      })

      "4mld" must {

        val max: Int = 51

        val is5mldEnabled: Boolean = false
        val isTaxable: Boolean = true

        "return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()

          val request = FakeRequest(GET, addAssetsRoute)

          val view = application.injector.instanceOf[MaxedOutView]

          val result = route(application, request).value

          status(result) mustEqual OK

          val content = contentAsString(result)

          content mustEqual
            view(fakeDraftId, Nil, assets(max), "You have added 51 assets", max)(request, messages).toString

          content must include("You cannot add another asset as you have entered a maximum of 51.")
          content must include("You can add another asset by removing an existing one, or write to HMRC with details of any additional assets.")

          application.stop()
        }

        "redirect to next page and set AddAssetsPage to NoComplete for a POST" in {

          reset(registrationsRepository)
          when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

          val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()

          val request = FakeRequest(POST, completePostRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

          verify(registrationsRepository).set(uaCaptor.capture)(any(), any())
          uaCaptor.getValue.get(AddAssetsPage).get mustBe NoComplete

          application.stop()
        }
      }

      "5mld" when {

        "taxable" must {

          val max: Int = 76

          val is5mldEnabled: Boolean = true
          val isTaxable: Boolean = true

          "return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()

            val request = FakeRequest(GET, addAssetsRoute)

            val view = application.injector.instanceOf[MaxedOutView]

            val result = route(application, request).value

            status(result) mustEqual OK

            val content = contentAsString(result)

            content mustEqual
              view(fakeDraftId, Nil, assets(max), "You have added 76 assets", max)(request, messages).toString

            content must include("You cannot add another asset as you have entered a maximum of 76.")
            content must include("You can add another asset by removing an existing one, or write to HMRC with details of any additional assets.")

            application.stop()
          }

          "redirect to next page and set AddAssetsPage to NoComplete for a POST" in {

            reset(registrationsRepository)
            when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
            val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

            val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()

            val request = FakeRequest(POST, completePostRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

            verify(registrationsRepository).set(uaCaptor.capture)(any(), any())
            uaCaptor.getValue.get(AddAssetsPage).get mustBe NoComplete

            application.stop()
          }
        }

        "non-taxable" must {

          val max: Int = 25

          val is5mldEnabled: Boolean = true
          val isTaxable: Boolean = false

          "return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()

            val request = FakeRequest(GET, addAssetsRoute)

            val view = application.injector.instanceOf[MaxedOutView]

            val result = route(application, request).value

            status(result) mustEqual OK

            val content = contentAsString(result)

            content mustEqual
              view(fakeDraftId, Nil, assets(max), "You have added 25 assets", max)(request, messages).toString

            content must include("You cannot add another asset as you have entered a maximum of 25.")
            content must include("You can add another asset by removing an existing one, or write to HMRC with details of any additional assets.")

            application.stop()
          }

          "redirect to next page and set AddAssetsPage to NoComplete for a POST" in {

            reset(registrationsRepository)
            when(registrationsRepository.set(any())(any(), any())).thenReturn(Future.successful(true))
            val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

            val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()

            val request = FakeRequest(POST, completePostRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

            verify(registrationsRepository).set(uaCaptor.capture)(any(), any())
            uaCaptor.getValue.get(AddAssetsPage).get mustBe NoComplete

            application.stop()
          }
        }
      }
    }
  }
}
