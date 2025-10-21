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

package controllers.asset.noneeabusiness

import base.SpecBase
import config.annotations.{Assets => AssetsAnnotations}
import connectors.TrustsStoreConnector
import forms.{AddAssetsFormProvider, YesNoFormProvider}
import generators.Generators
import models.Constants.MAX_NON_EEA_BUSINESS_ASSETS
import models.TaskStatus.Completed
import models.assets._
import models.{AddAssets, NormalMode, RemoveAsset, UkAddress}
import navigation.Navigator
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.AddAssetViewHelper
import viewmodels.{AddRow, AddToRows}
import views.html.asset.noneeabusiness.{AddAnAssetYesNoView, AddNonEeaBusinessAssetView, MaxedOutView}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class AddNonEeaBusinessAssetControllerSpec extends SpecBase with Generators with BeforeAndAfterEach {

  lazy val addAssetsRoute: String = controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad().url
  lazy val addOnePostRoute: String = controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.submitOne().url
  lazy val addAnotherPostRoute: String = controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.submitAnother().url
  lazy val completePostRoute: String = controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.submitComplete().url

  val prefix = "addNonEeaBusinessAsset"
  val AddNonEeaBusinessAssetForm: Form[AddAssets] = new AddAssetsFormProvider().withPrefix(prefix)
  val yesNoForm: Form[Boolean] = new YesNoFormProvider().withPrefix("addNonEeaBusinessAssetYesNo")

  val mockStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
  val mockViewHelper: AddAssetViewHelper = mock[AddAssetViewHelper]

  val fakeAddRow: AddRow = AddRow("Name", "Type", "change-url", "remove-url")

  val nonEeaBusiness: NonEeaBusinessType = NonEeaBusinessType(
    lineNo = None,
    orgName = "Business",
    address = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB"),
    govLawCountry = "GB",
    startDate = LocalDate.parse("2020-01-01"),
    endDate = None,
    provisional = false
  )

  override def beforeEach(): Unit = {
    reset(mockStoreConnector)
    reset(mockViewHelper)
    when(mockStoreConnector.updateTaskStatus(any(), any())(any(), any()))
      .thenReturn(Future.successful(HttpResponse(OK, "")))
  }

  "AddNonEeaBusinessAssetController Controller" when {

    "no data" must {
      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None)
          .build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None)
          .build()

        val request = FakeRequest(POST, addOnePostRoute)
          .withFormUrlEncodedBody(("value", AddAssets.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad.url

        application.stop()
      }
    }

    "there are no assets" must {

      val assets: Assets = Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil)
      val fakeService: FakeService = new FakeService(assets)

      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService)
          ).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.asset.routes.TrustOwnsNonEeaBusinessYesNoController.onPageLoad(NormalMode).url

        application.stop()
      }

      "redirect to the next page when valid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, addOnePostRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.asset.noneeabusiness.routes.NameController.onPageLoad(index, NormalMode).url

        application.stop()
      }

      "redirect to task list when no submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
          ).build()

        val request = FakeRequest(POST, addOnePostRoute)
          .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual frontendAppConfig.maintainATrustOverview

        verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, addOnePostRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = yesNoForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddAnAssetYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm)(request, messages).toString

        application.stop()
      }
    }

    "there is one asset" must {

      val assets: Assets = Assets(Nil, Nil, Nil, Nil, Nil, Nil, List(nonEeaBusiness))
      val fakeService: FakeService = new FakeService(assets)
      val fakeAddRows = fakeAddRow :: Nil

      "return OK and the correct view for a GET" in {

        when(mockViewHelper.rows(any(), any())(any())).thenReturn(AddToRows(fakeAddRows))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isTaxable = true)))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
          ).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddNonEeaBusinessAssetView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(AddNonEeaBusinessAssetForm, fakeAddRows, "Completed")(request, messages).toString

        verify(mockViewHelper).rows(eqTo(assets), eqTo(true))(any())

        application.stop()
      }
    }

    "there are existing assets" must {

      val numberOfAssets = 3
      val assets: Assets = Assets(Nil, Nil, Nil, Nil, Nil, Nil, List.fill(numberOfAssets)(nonEeaBusiness))
      val fakeService: FakeService = new FakeService(assets)
      val fakeAddRows = List.fill(numberOfAssets)(fakeAddRow)

      "return OK and the correct view for a GET" in {

        when(mockViewHelper.rows(any(), any())(any())).thenReturn(AddToRows(fakeAddRows))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isTaxable = true)))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
          ).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddNonEeaBusinessAssetView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(AddNonEeaBusinessAssetForm, fakeAddRows, s"You have added $numberOfAssets companies")(request, messages).toString

        verify(mockViewHelper).rows(eqTo(assets), eqTo(true))(any())

        application.stop()
      }

      "redirect to the next page when YesNow is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isTaxable = true)))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService)
          ).build()

        val request = FakeRequest(POST, addAnotherPostRoute)
          .withFormUrlEncodedBody(("value", AddAssets.YesNow.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.asset.noneeabusiness.routes.NameController.onPageLoad(numberOfAssets, NormalMode).url

        application.stop()
      }

      "redirect to task list when NoComplete is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
          ).build()

        val request = FakeRequest(POST, addAnotherPostRoute)
          .withFormUrlEncodedBody(("value", AddAssets.NoComplete.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual frontendAppConfig.maintainATrustOverview

        verify(mockStoreConnector).updateTaskStatus(any(), eqTo(Completed))(any(), any())

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        when(mockViewHelper.rows(any(), any())(any())).thenReturn(AddToRows(fakeAddRows))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
          ).build()

        val request = FakeRequest(POST, addAnotherPostRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = AddNonEeaBusinessAssetForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddNonEeaBusinessAssetView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(boundForm, fakeAddRows, s"You have added $numberOfAssets companies")(request, messages).toString

        verify(mockViewHelper).rows(eqTo(assets), eqTo(true))(any())

        application.stop()
      }
    }

    "maxed out assets" must {

      "return OK and the correct view for a GET" in {

        val assets: Assets = Assets(
          monetary = Nil,
          propertyOrLand = Nil,
          shares = Nil,
          business = Nil,
          partnerShip = Nil,
          other = Nil,
          nonEEABusiness = List.fill(MAX_NON_EEA_BUSINESS_ASSETS)(nonEeaBusiness)
        )
        val fakeService: FakeService = new FakeService(assets)
        val fakeAddRows = List.fill(MAX_NON_EEA_BUSINESS_ASSETS)(fakeAddRow)

        when(mockViewHelper.rows(any(), any())(any())).thenReturn(AddToRows(fakeAddRows))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
          .overrides(
            bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator),
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
          ).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaxedOutView]

        val content = contentAsString(result)

        content mustEqual
          view(fakeAddRows, s"You have added $MAX_NON_EEA_BUSINESS_ASSETS companies", MAX_NON_EEA_BUSINESS_ASSETS, prefix)(request, messages).toString

        content must include("You cannot add another company outside the UK or EEA as you have entered a maximum of 25.")
        content must include("You can add another company by removing an existing one, or write to HMRC with details of any additional companies outside the UK or EEA.")

        verify(mockViewHelper).rows(eqTo(assets), eqTo(true))(any())

        application.stop()
      }
    }
  }

  class FakeService(testAssets: Assets) extends TrustService {

    override def getAssets(identifier: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Assets] =
      Future.successful(testAssets)

    override def getMonetaryAsset(identifier: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Option[AssetMonetaryAmount]] =
      Future.successful(Some(testAssets.monetary.head))

    override def getPropertyOrLandAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[PropertyLandType] =
      Future.successful(testAssets.propertyOrLand(index))

    override def getSharesAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[SharesType] =
      Future.successful(testAssets.shares(index))

    override def getBusinessAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[BusinessAssetType] =
      Future.successful(testAssets.business(index))

    override def getOtherAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[OtherAssetType] =
      Future.successful(testAssets.other(index))

    override def getPartnershipAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[PartnershipType] =
      Future.successful(testAssets.partnerShip(index))

    override def getNonEeaBusinessAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[NonEeaBusinessType] =
      Future.successful(testAssets.nonEEABusiness(index))

    override def removeAsset(identifier: String, asset: RemoveAsset)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
      Future.successful(HttpResponse(OK, ""))
  }
}
