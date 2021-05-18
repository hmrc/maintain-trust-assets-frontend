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

import java.time.LocalDate

import base.SpecBase
import config.annotations.{Assets => AssetsAnnotations}
import connectors.TrustsStoreConnector
import controllers.asset.noneeabusiness
import forms.{AddAssetsFormProvider, YesNoFormProvider}
import generators.Generators
import models.assets._
import models.{AddAssets, CheckMode, NonUkAddress, RemoveAsset}
import navigation.Navigator
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import viewmodels.AddRow
import views.html.asset.nonTaxableToTaxable.{AddAssetYesNoView, AddAssetsView, MaxedOutView}

import scala.concurrent.{ExecutionContext, Future}

class AddAssetsControllerSpec extends SpecBase with Generators {

  lazy val addAssetsRoute: String = controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad().url
  lazy val addOnePostRoute: String = controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.submitOne().url
  lazy val addAnotherPostRoute: String = controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.submitAnother().url
  lazy val completePostRoute: String = controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.submitComplete().url

  def changeMoneyAssetRoute(): String =
    controllers.asset.money.routes.AssetMoneyValueController.onPageLoad(mode = CheckMode).url

  def removeMoneyAssetYesNoRoute(): String =
    controllers.asset.money.remove.routes.RemoveAssetYesNoController.onPageLoad().url

  def changeNonEeaAssetRoute(index: Int): String =
    noneeabusiness.amend.routes.AnswersController.extractAndRender(index).url

  def removeNoneEeaAssetYesNoRoute(index: Int): String =
    controllers.asset.noneeabusiness.remove.routes.RemoveAssetYesNoController.onPageLoad(index).url

  val prefix = "nonTaxableToTaxable.addAssets"
  val AddAssetForm: Form[AddAssets] = new AddAssetsFormProvider().withPrefix(prefix)
  val yesNoForm: Form[Boolean] = new YesNoFormProvider().withPrefix("addAssetsYesNo")

  val addRow1 = AddRow("orgName 1", typeLabel = "Non-EEA Company", changeNonEeaAssetRoute(0), removeNoneEeaAssetYesNoRoute(0))
  val addRow2 = AddRow("orgName 2", typeLabel = "Non-EEA Company", changeNonEeaAssetRoute(1), removeNoneEeaAssetYesNoRoute(1))
  val addRow3 = AddRow("£4800", typeLabel = "Money", changeMoneyAssetRoute(), removeMoneyAssetYesNoRoute())

  lazy val oneAsset: List[AddRow] = List(addRow1)
  lazy val multipleAssets: List[AddRow] = List(addRow1, addRow2, addRow3)

  val mockStoreConnector : TrustsStoreConnector = mock[TrustsStoreConnector]
  val nonEeaBusinessAsset1 = NonEeaBusinessType(None, "orgName 1", NonUkAddress("", "", None, ""), "", LocalDate.now, None, true)
  val nonEeaBusinessAsset2 = NonEeaBusinessType(None, "orgName 2", NonUkAddress("", "", None, ""), "", LocalDate.now, None, true)
  val moneyAsset = AssetMonetaryAmount(4800)

  val fakeEmptyService = new FakeService(Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
  val fakeServiceWithOneNonEeaAsset = new FakeService(Assets(Nil, Nil, Nil, Nil, Nil, Nil, List(nonEeaBusinessAsset1)))
  val fakeServiceWithMultipleNonEeaAssets = new FakeService(Assets(List(moneyAsset), Nil, Nil, Nil, Nil, Nil, List(nonEeaBusinessAsset1, nonEeaBusinessAsset2)))

  val nonEeaBusinessAsset = NonEeaBusinessType(None, "orgName", NonUkAddress("", "", None, ""), "", LocalDate.now, None, true)

  "AddAssets Controller" when {

    "no data" must {
      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeEmptyService),
          bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
        )).build()

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

    "there are no assets" when {

      "redirect to Session Expired for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeEmptyService),
          bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
        )).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

        application.stop()
      }

      "redirect to the next page when valid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeEmptyService)
        )).build()

        val request =
          FakeRequest(POST, addOnePostRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeServiceWithOneNonEeaAsset)
        )).build()

        val request =
          FakeRequest(POST, addOnePostRoute)
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

    "there is one asset" when {

      "return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
            .overrides(
              bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator),
              bind(classOf[TrustService]).toInstance(fakeServiceWithOneNonEeaAsset))
            .build()

          val request = FakeRequest(GET, addAssetsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AddAssetsView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(AddAssetForm, Nil, oneAsset, "Add assets")(request, messages).toString

          application.stop()

      }
    }

    "there are existing assets" when {

      "return OK and the correct view for a GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
            .overrides(
              bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator),
              bind(classOf[TrustService]).toInstance(fakeServiceWithMultipleNonEeaAssets))
            .build()

          val request = FakeRequest(GET, addAssetsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[AddAssetsView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(AddAssetForm, Nil, multipleAssets, "You have added 3 assets")(request, messages).toString

          application.stop()
        }
      }

      "redirect to the next page when YesNow is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
          .overrides(
            bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator),
            bind(classOf[TrustService]).toInstance(fakeServiceWithMultipleNonEeaAssets),
            bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector))
          .build()

        val request = FakeRequest(POST, addAnotherPostRoute)
          .withFormUrlEncodedBody(("value", AddAssets.YesNow.toString))

        when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse(200, "")))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url
        application.stop()
      }

      "redirect to the maintain task list when the user says they are done" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeServiceWithMultipleNonEeaAssets),
          bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector),
          bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator)
        )).build()

        val request =
          FakeRequest(POST, addAnotherPostRoute)
            .withFormUrlEncodedBody(("value", AddAssets.NoComplete.toString))

        when(mockStoreConnector.setTaskComplete(any())(any(), any())).thenReturn(Future.successful(HttpResponse(200, "")))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

        application.stop()
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeServiceWithMultipleNonEeaAssets)
        )).build()

        val request =
          FakeRequest(POST, addAnotherPostRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = AddAssetForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AddAssetsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual view(boundForm, Nil, multipleAssets, "You have added 3 assets")(request, messages).toString

        application.stop()
      }


    "assets maxed out" must {
      val max = 26
      val maxNonEeaAssets = 25

      def createNonEeaAsset(max: Int): List[NonEeaBusinessType] = 0.until(max).foldLeft[List[NonEeaBusinessType]](List())((acc, i) => {
        acc :+ NonEeaBusinessType(None, s"orgName $i", NonUkAddress("", "", None, ""), "", LocalDate.now, None, true)
      })

      def createNonEeaAssetRows(max: Int): List[AddRow] = 0.until(max).foldLeft[List[AddRow]](List())((acc, i) => {
        acc :+ AddRow(s"orgName $i", typeLabel = "Non-EEA Company", changeNonEeaAssetRoute(i), removeNoneEeaAssetYesNoRoute(i))
      })

      val assets = Assets(List(moneyAsset), Nil, Nil, Nil, Nil, Nil, createNonEeaAsset(maxNonEeaAssets))
      val assetRows: List[AddRow] = createNonEeaAssetRows(maxNonEeaAssets) ++
        List(AddRow(s"£${moneyAsset.assetMonetaryAmount}", "Money", changeMoneyAssetRoute(), removeMoneyAssetYesNoRoute()))

      val fakeService = new FakeService(assets)

      "return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isMigratingToTaxable = true)))
          .overrides(bind(classOf[TrustService]).toInstance(fakeService))
          .build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MaxedOutView]

        status(result) mustEqual OK

        val content = contentAsString(result)

        content mustEqual
          view(Nil, assetRows, s"You have added 26 assets", max, prefix)(request, messages).toString

        content must include("You cannot add another asset as you have entered a maximum of 26.")
        content must include("You can add another asset by removing an existing one, or write to HMRC with details of any additional assets.")

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

