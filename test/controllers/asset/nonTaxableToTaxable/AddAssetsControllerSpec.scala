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

package controllers.asset.nonTaxableToTaxable

import base.SpecBase
import config.annotations.{Assets => AssetsAnnotations}
import connectors.TrustsStoreConnector
import forms.{AddAssetsFormProvider, YesNoFormProvider}
import generators.Generators
import models.Constants._
import models.assets._
import models.{AddAssets, RemoveAsset, ShareClass, UkAddress}
import navigation.{AssetsNavigator, Navigator}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.AddAssetViewHelper
import utils.Constants.QUOTED
import viewmodels.{AddRow, AddToRows}
import views.html.asset.nonTaxableToTaxable.{AddAssetYesNoView, AddAssetsView, MaxedOutView}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class AddAssetsControllerSpec extends SpecBase with Generators with BeforeAndAfterEach {

  lazy val addAssetsRoute: String = controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url
  lazy val addOnePostRoute: String = controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.submitOne().url
  lazy val addAnotherPostRoute: String = controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.submitAnother().url
  lazy val completePostRoute: String = controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.submitComplete().url

  val prefix = "nonTaxableToTaxable.addAssets"
  val addAssetForm: Form[AddAssets] = new AddAssetsFormProvider().withPrefix(prefix)
  val yesNoForm: Form[Boolean] = new YesNoFormProvider().withPrefix("addAssetsYesNo")

  val mockStoreConnector: TrustsStoreConnector = mock[TrustsStoreConnector]
  val mockViewHelper: AddAssetViewHelper = mock[AddAssetViewHelper]
  val mockNavigator: AssetsNavigator = mock[AssetsNavigator]

  val fakeAddRow: AddRow = AddRow("Name", "Type", "change-url", "remove-url")

  val assetValue: Long = 4000L

  val money: AssetMonetaryAmount = AssetMonetaryAmount(assetMonetaryAmount = assetValue)

  val propertyOrLand: PropertyLandType = PropertyLandType(
    buildingLandName = None,
    address = None,
    valueFull = assetValue,
    valuePrevious = None
  )

  val share: SharesType = SharesType(
    numberOfShares = "1",
    orgName = "Share",
    shareClass = ShareClass.Ordinary.toString,
    typeOfShare = QUOTED,
    value = assetValue,
    isPortfolio = None
  )

  val business: BusinessAssetType = BusinessAssetType(
    orgName = "Business",
    businessDescription = "Description",
    address = UkAddress("Line 1", "Line 2", None, None, "AB1 1AB"),
    businessValue = assetValue
  )

  val partnership: PartnershipType = PartnershipType(
    description = "Description",
    partnershipStart = LocalDate.parse("2020-01-01")
  )

  val other: OtherAssetType = OtherAssetType(
    description = "Description",
    value = assetValue
  )

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
    reset(mockNavigator, mockStoreConnector, mockViewHelper)

    when(mockNavigator.addAssetRoute(any())).thenReturn(fakeNavigator.desiredRoute)
    when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse(200, "")))
  }

  "AddAssets Controller" when {

    "no data" must {
      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None)
          .build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "redirect to Session Expired for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None)
          .build()

        val request = FakeRequest(POST, addAssetsRoute)
          .withFormUrlEncodedBody(("value", AddAssets.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }
    }

    "there are no assets" must {

      val assets: Assets = Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil)
      val fakeService: FakeService = new FakeService(assets)

      "redirect to AddAssetsYesNoController for a GET" in {

        when(mockViewHelper.rows(any())(any())).thenReturn(AddToRows(Nil))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
          ).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.asset.nonTaxableToTaxable.routes.AddAssetYesNoController.onPageLoad().url

        verify(mockViewHelper).rows(eqTo(assets))(any())

        application.stop()
      }

      "redirect to the next page when yes submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, addOnePostRoute)
          .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.asset.routes.WhatKindOfAssetController.onPageLoad().url

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

        verify(mockStoreConnector).setTaskComplete(any())(any(), any())

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(POST, addOnePostRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = yesNoForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddAssetYesNoView]

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

        when(mockViewHelper.rows(any())(any())).thenReturn(AddToRows(fakeAddRows))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
          ).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAssetsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(addAssetForm, fakeAddRows, "Add assets")(request, messages).toString

        verify(mockViewHelper).rows(eqTo(assets))(any())

        application.stop()

      }
    }

    "there are existing assets" when {

      val numberOfAssets = 3
      val assets: Assets = Assets(Nil, Nil, Nil, Nil, Nil, Nil, List.fill(numberOfAssets)(nonEeaBusiness))
      val fakeService: FakeService = new FakeService(assets)
      val fakeAddRows = List.fill(numberOfAssets)(fakeAddRow)

      "return OK and the correct view for a GET" in {

        when(mockViewHelper.rows(any())(any())).thenReturn(AddToRows(fakeAddRows))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
          .overrides(
            bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator),
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
          ).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddAssetsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(addAssetForm, fakeAddRows, s"You have added $numberOfAssets assets")(request, messages).toString

        verify(mockViewHelper).rows(eqTo(assets))(any())

        application.stop()
      }

      "redirect to the next page when YesNow is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[AssetsNavigator]).toInstance(mockNavigator)
          ).build()

        val request = FakeRequest(POST, addAnotherPostRoute)
          .withFormUrlEncodedBody(("value", AddAssets.YesNow.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

        verify(mockNavigator).addAssetRoute(assets)

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

        verify(mockStoreConnector).setTaskComplete(any())(any(), any())

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        when(mockViewHelper.rows(any())(any())).thenReturn(AddToRows(fakeAddRows))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustService]).toInstance(fakeService),
            bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
          ).build()

        val request = FakeRequest(POST, addAnotherPostRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = addAssetForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddAssetsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(boundForm, fakeAddRows, s"You have added $numberOfAssets assets")(request, messages).toString

        verify(mockViewHelper).rows(eqTo(assets))(any())

        application.stop()
      }
    }

    "maxed out assets" must {

      "return OK and the correct view for a GET" when {

        "one type maxed out" in {

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

          when(mockViewHelper.rows(any())(any())).thenReturn(AddToRows(fakeAddRows))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
            .overrides(
              bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator),
              bind(classOf[TrustService]).toInstance(fakeService),
              bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
            ).build()

          val request = FakeRequest(GET, addAssetsRoute)

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) must include("You cannot add another Non-EEA Company as you have entered a maximum of 25.")
          contentAsString(result) must include("If you have further assets to add within this type, write to HMRC with their details.")

          verify(mockViewHelper).rows(eqTo(assets))(any())

          application.stop()
        }

        "more than one type maxed out" in {

          val assets: Assets = Assets(
            monetary = List.fill(MAX_MONEY_ASSETS)(money),
            propertyOrLand = Nil,
            shares = Nil,
            business = Nil,
            partnerShip = Nil,
            other = Nil,
            nonEEABusiness = List.fill(MAX_NON_EEA_BUSINESS_ASSETS)(nonEeaBusiness)
          )
          val fakeService: FakeService = new FakeService(assets)
          val fakeAddRows = List.fill(MAX_MONEY_ASSETS + MAX_NON_EEA_BUSINESS_ASSETS)(fakeAddRow)

          when(mockViewHelper.rows(any())(any())).thenReturn(AddToRows(fakeAddRows))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
            .overrides(
              bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator),
              bind(classOf[TrustService]).toInstance(fakeService),
              bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
            ).build()

          val request = FakeRequest(GET, addAssetsRoute)

          val result = route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) must include("You have entered the maximum number of assets for:")
          contentAsString(result) must include("If you have further assets to add within these types, write to HMRC with their details.")

          verify(mockViewHelper).rows(eqTo(assets))(any())

          application.stop()
        }

        "all types maxed out" in {

          val assets: Assets = Assets(
            monetary = List.fill(MAX_MONEY_ASSETS)(money),
            propertyOrLand = List.fill(MAX_PROPERTY_OR_LAND_ASSETS)(propertyOrLand),
            shares = List.fill(MAX_SHARES_ASSETS)(share),
            business = List.fill(MAX_BUSINESS_ASSETS)(business),
            partnerShip = List.fill(MAX_PARTNERSHIP_ASSETS)(partnership),
            other = List.fill(MAX_OTHER_ASSETS)(other),
            nonEEABusiness = List.fill(MAX_NON_EEA_BUSINESS_ASSETS)(nonEeaBusiness)
          )
          val fakeService: FakeService = new FakeService(assets)
          val fakeAddRows = List.fill(MAX_ALL_ASSETS)(fakeAddRow)

          when(mockViewHelper.rows(any())(any())).thenReturn(AddToRows(fakeAddRows))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
            .overrides(
              bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator),
              bind(classOf[TrustService]).toInstance(fakeService),
              bind(classOf[AddAssetViewHelper]).toInstance(mockViewHelper)
            ).build()

          val request = FakeRequest(GET, addAssetsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[MaxedOutView]

          status(result) mustEqual OK

          val content = contentAsString(result)

          content mustEqual view(fakeAddRows, "The trust has 76 assets", 76, prefix)(request, messages).toString
          content must include("You cannot enter another asset as you have entered a maximum of 76.")
          content must include("If you have further assets to add, write to HMRC with their details.")

          verify(mockViewHelper).rows(eqTo(assets))(any())

          application.stop()
        }
      }

      "redirect to task list for a POST" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
          ).build()

        val request = FakeRequest(POST, completePostRoute)
          .withFormUrlEncodedBody(("value", AddAssets.NoComplete.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual frontendAppConfig.maintainATrustOverview

        verify(mockStoreConnector).setTaskComplete(any())(any(), any())

        application.stop()
      }
    }
  }


  class FakeService(testAssets: Assets) extends TrustService {

    override def getAssets(identifier: String)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[Assets] =
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
