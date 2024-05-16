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

package controllers.asset.noneeabusiness.remove

import base.SpecBase
import connectors.TrustsConnector
import forms.EndDateFormProvider
import models.{NonUkAddress, UserAnswers}
import models.assets._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.asset.noneeabusiness.remove.RemoveAssetEndDateView

import java.time.LocalDate
import scala.concurrent.Future

class RemoveAssetEndDateControllerSpec extends SpecBase with ScalaCheckPropertyChecks with ScalaFutures {


  lazy val formRoute: Call = routes.RemoveAssetEndDateController.onSubmit(0)

  private val assetStartDate: LocalDate = LocalDate.parse("1996-02-03")
  private val validAnswer: LocalDate = LocalDate.parse("1996-02-03")
  private val toEarlyDate: LocalDate = LocalDate.parse("1996-02-02")

  private val formProvider = new EndDateFormProvider()
  private val prefix: String = "nonEeaBusiness.endDate"
  private val form = formProvider.withConfig(prefix, assetStartDate)

  val mockConnector: TrustsConnector = mock[TrustsConnector]

  def createAsset(id: Int, provisional: Boolean): NonEeaBusinessType =
    NonEeaBusinessType(None, s"OrgName $id", NonUkAddress("", "", None, ""), "", assetStartDate, None, provisional)

  val nonEeaAssets: List[NonEeaBusinessType] = List(
    createAsset(0, provisional = false),
    createAsset(1, provisional = true),
    createAsset(2, provisional = true)
  )

  def userAnswers(migrating: Boolean): UserAnswers = emptyUserAnswers.copy(isMigratingToTaxable = migrating)

  "RemoveAssetEndDateController" when {

    "return OK and the correct view for a GET" in {

      val index = 0

      when(mockConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(Assets(Nil, Nil, Nil, Nil, Nil, Nil, nonEeaAssets)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustsConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, routes.RemoveAssetEndDateController.onPageLoad(index).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemoveAssetEndDateView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, index, s"OrgName $index")(request, messages).toString

      application.stop()
    }

    "removing a new asset" must {

      "redirect to the 'add non-eea asset' page, removing the asset when not migrating" in {

        val index = 2

        val answers = userAnswers(migrating = false)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        when(mockConnector.getAssets(any())(any(), any()))
          .thenReturn(Future.successful(Assets(Nil, Nil, Nil, Nil, Nil, Nil, nonEeaAssets)))

        when(mockConnector.removeAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))

        val request =
          FakeRequest(POST, routes.RemoveAssetEndDateController.onSubmit(index).url)
            .withFormUrlEncodedBody(
              "value.day" -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year" -> validAnswer.getYear.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad().url

        application.stop()
      }

      "redirect to the 'add asset' page, removing the asset when migrating" in {

        val index = 2

        val answers = userAnswers(migrating = true)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        when(mockConnector.getAssets(any())(any(), any()))
          .thenReturn(Future.successful(Assets(Nil, Nil, Nil, Nil, Nil, Nil, nonEeaAssets)))

        when(mockConnector.removeAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))

        val request =
          FakeRequest(POST, routes.RemoveAssetEndDateController.onSubmit(index).url)
            .withFormUrlEncodedBody(
              "value.day" -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year" -> validAnswer.getYear.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

        application.stop()
      }
    }

    "removing an old asset" must {

      "redirect to the 'add non-eea asset' page, removing the asset and not migrating" in {

        val index = 0

        val answers = userAnswers(migrating = false)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        when(mockConnector.getAssets(any())(any(), any()))
          .thenReturn(Future.successful(Assets(Nil, Nil, Nil, Nil, Nil, Nil, nonEeaAssets)))

        when(mockConnector.removeAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))

        val request =
          FakeRequest(POST, routes.RemoveAssetEndDateController.onSubmit(index).url)
            .withFormUrlEncodedBody(
              "value.day" -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year" -> validAnswer.getYear.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad().url

        application.stop()
      }

      "redirect to the 'add asset' page, removing the asset when migrating" in {

        val index = 0

        val answers = userAnswers(migrating = true)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        when(mockConnector.getAssets(any())(any(), any()))
          .thenReturn(Future.successful(Assets(Nil, Nil, Nil, Nil, Nil, Nil, nonEeaAssets)))

        when(mockConnector.removeAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))

        val request =
          FakeRequest(POST, routes.RemoveAssetEndDateController.onSubmit(index).url)
            .withFormUrlEncodedBody(
              "value.day" -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year" -> validAnswer.getYear.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

        application.stop()
      }
    }

    "return a Bad Request and errors when the entered date is before the asset start date" in {
      val index = 0
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustsConnector].toInstance(mockConnector))
        .build()

      when(mockConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(Assets(Nil, Nil, Nil, Nil, Nil, Nil, nonEeaAssets)))

      val request =
        FakeRequest(POST, routes.RemoveAssetEndDateController.onSubmit(index).url)
          .withFormUrlEncodedBody(
            "value.day" -> toEarlyDate.getDayOfMonth.toString,
            "value.month" -> toEarlyDate.getMonthValue.toString,
            "value.year" -> toEarlyDate.getYear.toString
          )

      val boundForm = form.bind(Map(
        "value.day" -> toEarlyDate.getDayOfMonth.toString,
        "value.month" -> toEarlyDate.getMonthValue.toString,
        "value.year" -> toEarlyDate.getYear.toString
      ))

      val view = application.injector.instanceOf[RemoveAssetEndDateView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, s"OrgName $index")(request, messages).toString

      application.stop()
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val index = 0

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(bind[TrustsConnector].toInstance(mockConnector)).build()

      val request =
        FakeRequest(POST, routes.RemoveAssetEndDateController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", "invalid value"))

      val boundForm = form.bind(Map("value" -> "invalid value"))

      val view = application.injector.instanceOf[RemoveAssetEndDateView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, s"OrgName $index")(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, routes.RemoveAssetEndDateController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, routes.RemoveAssetEndDateController.onSubmit(index).url)
          .withFormUrlEncodedBody(
            "value.day" -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year" -> validAnswer.getYear.toString
          )

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
