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

package controllers.asset.partnership.remove

import base.SpecBase
import connectors.TrustsConnector
import forms.RemoveIndexFormProvider
import models.UserAnswers
import models.assets._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import views.html.asset.partnership.RemovePartnershipAssetYesNoView

import java.time.LocalDate
import scala.concurrent.Future

class RemovePartnershipAssetYesNoControllerSpec extends SpecBase with ScalaCheckPropertyChecks with ScalaFutures {

  val messagesPrefix = "partnership.removeYesNo"

  lazy val formProvider = new RemoveIndexFormProvider()
  lazy val form: Form[Boolean] = formProvider(messagesPrefix)

  lazy val formRoute: Call = controllers.asset.partnership.remove.routes.RemovePartnershipAssetYesNoController.onSubmit(0)

  val mockConnector: TrustsConnector = mock[TrustsConnector]

  def createAsset(id: Int, provisional: Boolean): PartnershipType =
    PartnershipType(s"Partnership Asset $id", LocalDate.now())

  val assets: List[PartnershipType] = List(
    createAsset(0, provisional = false),
    createAsset(1, provisional = true),
    createAsset(2, provisional = true)
  )

  def userAnswers(migrating: Boolean): UserAnswers = emptyUserAnswers.copy(isMigratingToTaxable = migrating)

  "Other RemovePartnershipAssetYesNo Controller" when {

    "return OK and the correct view for a GET" in {

      val index = 0

      when(mockConnector.getAssets(any())(any(), any()))
        .thenReturn(Future.successful(Assets(Nil, Nil, Nil, Nil, assets, Nil, Nil)))

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustsConnector].toInstance(mockConnector))
        .build()

      val request = FakeRequest(GET, controllers.asset.partnership.remove.routes.RemovePartnershipAssetYesNoController.onPageLoad(index).url)

      val result = route(application, request).value

      val view = application.injector.instanceOf[RemovePartnershipAssetYesNoView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual view(form, index, s"Partnership Asset $index")(request, messages).toString

      application.stop()
    }

    "not removing the asset" must {

      "redirect to the 'add asset' page when valid data is submitted and migrating" in {

        val index = 0

        val answers = userAnswers(migrating = true)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        val request =
          FakeRequest(POST, controllers.asset.partnership.remove.routes.RemovePartnershipAssetYesNoController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

        application.stop()
      }
    }

    "removing an old asset" must {

      "redirect to the 'add asset' page, removing the asset when migrating" in {

        val index = 0

        val answers = userAnswers(migrating = true)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[TrustsConnector].toInstance(mockConnector))
          .build()

        when(mockConnector.getAssets(any())(any(), any()))
          .thenReturn(Future.successful(Assets(Nil, Nil, Nil, Nil, assets, Nil, Nil)))

        when(mockConnector.removeAsset(any(), any())(any(), any()))
          .thenReturn(Future.successful(HttpResponse(200, "")))

        val request =
          FakeRequest(POST, controllers.asset.partnership.remove.routes.RemovePartnershipAssetYesNoController.onSubmit(index).url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url

        application.stop()
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      val index = 0

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[TrustsConnector].toInstance(mockConnector))
        .build()

      val request =
        FakeRequest(POST, controllers.asset.partnership.remove.routes.RemovePartnershipAssetYesNoController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", ""))

      val boundForm = form.bind(Map("value" -> ""))

      val view = application.injector.instanceOf[RemovePartnershipAssetYesNoView]

      val result = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(boundForm, index, s"Partnership Asset $index")(request, messages).toString

      application.stop()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, controllers.asset.partnership.remove.routes.RemovePartnershipAssetYesNoController.onPageLoad(index).url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }

    "redirect to Session Expired for a POST if no existing data is found" in {

      val index = 0

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, controllers.asset.partnership.remove.routes.RemovePartnershipAssetYesNoController.onSubmit(index).url)
          .withFormUrlEncodedBody(("value", "true"))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

      application.stop()
    }
  }
}
