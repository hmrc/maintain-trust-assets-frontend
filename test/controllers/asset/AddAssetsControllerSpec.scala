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

import java.time.LocalDate

import base.SpecBase
import config.annotations.{Assets => AssetsAnnotations}
import connectors.TrustsStoreConnector
import forms.{AddAssetsFormProvider, YesNoFormProvider}
import generators.Generators
import javax.inject.Inject
import models.AddAssets.{NoComplete, YesNow}
import models.Status.Completed
import models.WhatKindOfAsset.{Money, NonEeaBusiness, Other, Shares}
import models.{AddAssets, AddressType, AssetMonetaryAmount, Assets, BusinessAssetType, NonEeaBusinessType, NormalMode, OtherAssetType, PartnershipType, PropertyLandType, RemoveAsset, ShareClass, SharesType, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.AssetStatus
import pages.asset.money._
import pages.asset.other.OtherAssetDescriptionPage
import pages.asset.shares._
import pages.asset.{AddAnAssetYesNoPage, AddAssetsPage, WhatKindOfAssetPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.TrustService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import viewmodels.AddRow
import views.html.asset.{AddAnAssetYesNoView, AddAssetsView, MaxedOutView}

import scala.concurrent.{ExecutionContext, Future}

class AddAssetsControllerSpec extends SpecBase with Generators {

  lazy val addAssetsRoute: String = routes.AddAssetsController.onPageLoad().url
  lazy val addOnePostRoute: String = routes.AddAssetsController.submitOne().url
  lazy val addAnotherPostRoute: String = routes.AddAssetsController.submitAnother().url
  lazy val completePostRoute: String = routes.AddAssetsController.submitComplete().url

  def changeMoneyAssetRoute(index: Int): String =
    money.routes.AssetMoneyValueController.onPageLoad(NormalMode).url

  def changeSharesAssetRoute(index: Int): String =
    shares.routes.ShareAnswerController.onPageLoad().url

  def changeOtherAssetRoute(index: Int): String =
    other.routes.OtherAssetAnswersController.onPageLoad().url

  def removeAssetYesNoRoute(index: Int): String =
    "/foo"

  val addTaxableAssetsForm: Form[AddAssets] = new AddAssetsFormProvider().withPrefix("addAssets")
  val addNonTaxableAssetsForm: Form[AddAssets] = new AddAssetsFormProvider().withPrefix("addAssets.nonTaxable")
  val yesNoForm: Form[Boolean] = new YesNoFormProvider().withPrefix("addAnAssetYesNo")

  lazy val oneAsset: List[AddRow] = List(AddRow("£4800", typeLabel = "Money", changeMoneyAssetRoute(0), removeAssetYesNoRoute(0)))

  lazy val multipleAssets: List[AddRow] = oneAsset :+ AddRow("Share Company Name", typeLabel = "Shares", changeSharesAssetRoute(1), removeAssetYesNoRoute(1))

  val userAnswersWithOneAsset: UserAnswers = emptyUserAnswers
    .set(WhatKindOfAssetPage, Money).success.value
    .set(AssetMoneyValuePage, 4800L).success.value
    .set(AssetStatus, Completed).success.value

  val userAnswersWithMultipleAssets: UserAnswers = userAnswersWithOneAsset
    .set(WhatKindOfAssetPage, Shares).success.value
    .set(SharesInAPortfolioPage, false).success.value
    .set(ShareCompanyNamePage, "Share Company Name").success.value
    .set(SharesOnStockExchangePage, true).success.value
    .set(ShareClassPage, ShareClass.Ordinary).success.value
    .set(ShareQuantityInTrustPage, 1000L).success.value
    .set(ShareValueInTrustPage, 10L).success.value
    .set(AssetStatus, Completed).success.value

  val mockStoreConnector : TrustsStoreConnector = mock[TrustsStoreConnector]

  val nonEeaBusinessAsset = NonEeaBusinessType(None, "orgName", AddressType("", "", None, None, None, ""), "", LocalDate.now, None)

  "AddAssets Controller" when {

    "no data" must {
      "redirect to Session Expired for a GET if no existing data is found" in {

        val fakeService = new FakeService(Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))

        val application = applicationBuilder(userAnswers = None).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService),
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

      "redirect to TrustOwnsNonEeaBusinessYesNoController" in {
        val fakeService = new FakeService(Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
        val answers = emptyUserAnswers

        val application = applicationBuilder(userAnswers = Some(answers)).overrides(Seq(
          bind(classOf[TrustService]).toInstance(fakeService),
          bind(classOf[TrustsStoreConnector]).toInstance(mockStoreConnector)
        )).build()

        val request = FakeRequest(GET, addAssetsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.TrustOwnsNonEeaBusinessYesNoController.onPageLoad(NormalMode).url

        application.stop()
      }

      "redirect to the next page when valid data is submitted" when {

        "taxable" must {
          "set value in AddAnAssetYesNoPage" in {
            val fakeService = new FakeService(Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
            reset(playbackRepository)
            when(playbackRepository.set(any())).thenReturn(Future.successful(true))
            val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

            val application =
              applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isTaxable = true)))
                .overrides(
                  bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator),
                  bind(classOf[TrustService]).toInstance(fakeService))
                .build()

            val request = FakeRequest(POST, addOnePostRoute)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

            verify(playbackRepository).set(uaCaptor.capture)
            uaCaptor.getValue.get(AddAnAssetYesNoPage).get mustBe true
            uaCaptor.getValue.get(WhatKindOfAssetPage) mustNot be(defined)

            application.stop()
          }
        }

        "non-taxable" must {
          "set values in AddAnAssetYesNoPage and WhatKindOfAssetPage" in {
            val fakeService = new FakeService(Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil))
            reset(playbackRepository)
            when(playbackRepository.set(any())).thenReturn(Future.successful(true))
            val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

            val application =
              applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(isTaxable = false)))
                .overrides(
                  bind[Navigator].qualifiedWith(classOf[AssetsAnnotations]).toInstance(fakeNavigator),
                  bind(classOf[TrustService]).toInstance(fakeService))
                .build()

            val request = FakeRequest(POST, addOnePostRoute)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url

            verify(playbackRepository).set(uaCaptor.capture)
            uaCaptor.getValue.get(AddAnAssetYesNoPage).get mustBe true
            uaCaptor.getValue.get(WhatKindOfAssetPage).get mustBe NonEeaBusiness

            application.stop()
          }
        }
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
          view(boundForm)(request, messages).toString

        application.stop()
      }
    }

    // TODO

//    "there is one asset" must {
//
//      "return OK and the correct view for a GET" when {
//
//        "taxable" in {
//
//          val application = applicationBuilder(userAnswers = Some(userAnswersWithOneAsset.copy(isTaxable = true))).build()
//
//          val request = FakeRequest(GET, addAssetsRoute)
//
//          val result = route(application, request).value
//
//          val view = application.injector.instanceOf[AddAssetsView]
//
//          status(result) mustEqual OK
//
//          contentAsString(result) mustEqual
//            view(addTaxableAssetsForm, Nil, oneAsset, "Add assets", "addAssets")(request, messages).toString
//
//          application.stop()
//        }
//
//        "non-taxable" in {
//
//          val application = applicationBuilder(userAnswers = Some(userAnswersWithOneAsset.copy(isTaxable = false))).build()
//
//          val request = FakeRequest(GET, addAssetsRoute)
//
//          val result = route(application, request).value
//
//          val view = application.injector.instanceOf[AddAssetsView]
//
//          status(result) mustEqual OK
//
//          contentAsString(result) mustEqual
//            view(addNonTaxableAssetsForm, Nil, oneAsset, "Add a non-EEA company", "addAssets.nonTaxable")(request, messages).toString
//
//          application.stop()
//        }
//      }
//    }

//    "there are existing assets" must {
//
//      "return OK and the correct view for a GET" when {
//
//        "taxable" in {
//
//          val application = applicationBuilder(userAnswers = Some(userAnswersWithMultipleAssets.copy(isTaxable = true))).build()
//
//          val request = FakeRequest(GET, addAssetsRoute)
//
//          val result = route(application, request).value
//
//          val view = application.injector.instanceOf[AddAssetsView]
//
//          status(result) mustEqual OK
//
//          contentAsString(result) mustEqual
//            view(addTaxableAssetsForm, Nil, multipleAssets, "You have added 2 assets", "addAssets")(request, messages).toString
//
//          application.stop()
//        }
//
//        "non-taxable" in {
//
//          val application = applicationBuilder(userAnswers = Some(userAnswersWithMultipleAssets.copy(isTaxable = false))).build()
//
//          val request = FakeRequest(GET, addAssetsRoute)
//
//          val result = route(application, request).value
//
//          val view = application.injector.instanceOf[AddAssetsView]
//
//          status(result) mustEqual OK
//
//          contentAsString(result) mustEqual
//            view(addNonTaxableAssetsForm, Nil, multipleAssets, "You have added 2 non-EEA companies", "addAssets.nonTaxable")(request, messages).toString
//
//          application.stop()
//        }
//      }
//
//      "redirect to the next page when YesNow is submitted" when {
//
//        "taxable" must {
//          "set value in AddAssetsPage and not set value in WhatKindOfAssetPage" in {
//
//            reset(playbackRepository)
//            when(playbackRepository.set(any())).thenReturn(Future.successful(true))
//            val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
//
//            val application =
//              applicationBuilder(userAnswers = Some(userAnswersWithMultipleAssets.copy(isTaxable = true))).build()
//
//            val request = FakeRequest(POST, addAnotherPostRoute)
//              .withFormUrlEncodedBody(("value", YesNow.toString))
//
//            val result = route(application, request).value
//
//            status(result) mustEqual SEE_OTHER
//
//            redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url
//
//            verify(playbackRepository).set(uaCaptor.capture)
//            uaCaptor.getValue.get(AddAssetsPage).get mustBe YesNow
//            uaCaptor.getValue.get(WhatKindOfAssetPage) mustNot be(defined)
//
//            application.stop()
//          }
//        }
//
//        "non-taxable" must {
//          "set values in AddAssetsPage and WhatKindOfAssetPage" in {
//
//            reset(playbackRepository)
//            when(playbackRepository.set(any())).thenReturn(Future.successful(true))
//            val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
//
//            val application =
//              applicationBuilder(userAnswers = Some(userAnswersWithMultipleAssets.copy(isTaxable = false))).build()
//
//            val request = FakeRequest(POST, addAnotherPostRoute)
//              .withFormUrlEncodedBody(("value", YesNow.toString))
//
//            val result = route(application, request).value
//
//            status(result) mustEqual SEE_OTHER
//
//            redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url
//
//            verify(playbackRepository).set(uaCaptor.capture)
//            uaCaptor.getValue.get(AddAssetsPage).get mustBe YesNow
//            uaCaptor.getValue.get(WhatKindOfAssetPage).get mustBe NonEeaBusiness
//
//            application.stop()
//          }
//        }
//      }
//
//      "redirect to the next page when YesLater or NoComplete is submitted" when {
//
//        "taxable" must {
//          "set value in AddAssetsPage and not set value in WhatKindOfAssetPage" in {
//
//            forAll(arbitrary[AddAssets].filterNot(_ == YesNow)) {
//              addAssets =>
//
//                reset(playbackRepository)
//                when(playbackRepository.set(any())).thenReturn(Future.successful(true))
//                val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
//
//                val application =
//                  applicationBuilder(userAnswers = Some(userAnswersWithMultipleAssets.copy(isTaxable = true))).build()
//
//                val request = FakeRequest(POST, addAnotherPostRoute)
//                  .withFormUrlEncodedBody(("value", addAssets.toString))
//
//                val result = route(application, request).value
//
//                status(result) mustEqual SEE_OTHER
//
//                redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url
//
//                verify(playbackRepository).set(uaCaptor.capture)
//                uaCaptor.getValue.get(AddAssetsPage).get mustBe addAssets
//                uaCaptor.getValue.get(WhatKindOfAssetPage) mustNot be(defined)
//
//                application.stop()
//            }
//          }
//        }
//
//        "non-taxable" must {
//          "set value in AddAssetsPage and not set value in WhatKindOfAssetPage" in {
//
//            forAll(arbitrary[AddAssets].filterNot(_ == YesNow)) {
//              addAssets =>
//
//                reset(playbackRepository)
//                when(playbackRepository.set(any())).thenReturn(Future.successful(true))
//                val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
//
//                val application =
//                  applicationBuilder(userAnswers = Some(userAnswersWithMultipleAssets.copy(isTaxable = false))).build()
//
//                val request = FakeRequest(POST, addAnotherPostRoute)
//                  .withFormUrlEncodedBody(("value", addAssets.toString))
//
//                val result = route(application, request).value
//
//                status(result) mustEqual SEE_OTHER
//
//                redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url
//
//                verify(playbackRepository).set(uaCaptor.capture)
//                uaCaptor.getValue.get(AddAssetsPage).get mustBe addAssets
//                uaCaptor.getValue.get(WhatKindOfAssetPage) mustNot be(defined)
//
//                application.stop()
//            }
//          }
//        }
//      }
//
//      "return a Bad Request and errors when invalid data is submitted" in {
//
//        val application = applicationBuilder(userAnswers = Some(userAnswersWithMultipleAssets)).build()
//
//        val request =
//          FakeRequest(POST, addAnotherPostRoute)
//            .withFormUrlEncodedBody(("value", "invalid value"))
//
//        val boundForm = addTaxableAssetsForm.bind(Map("value" -> "invalid value"))
//
//        val view = application.injector.instanceOf[AddAssetsView]
//
//        val result = route(application, request).value
//
//        status(result) mustEqual BAD_REQUEST
//
//        contentAsString(result) mustEqual
//          view(boundForm, Nil, multipleAssets, "You have added 2 assets", "addAssets")(request, messages).toString
//
//        application.stop()
//      }
//    }

//    "assets maxed out" when {
//
//      val description: String = "Description"
//
//      def userAnswers(max: Int, is5mldEnabled: Boolean, isTaxable: Boolean): UserAnswers = {
//        0.until(max).foldLeft(emptyUserAnswers.copy(is5mldEnabled = is5mldEnabled, isTaxable = isTaxable))((ua, i) => {
//          ua
//            .set(WhatKindOfAssetPage, Other).success.value
//            .set(OtherAssetDescriptionPage, description).success.value
//            .set(AssetStatus, Completed).success.value
//        })
//      }
//
//      def assets(max: Int): List[AddRow] = 0.until(max).foldLeft[List[AddRow]](List())((acc, i) => {
//        acc :+ AddRow(description, typeLabel = "Other", changeOtherAssetRoute(i), removeAssetYesNoRoute(i))
//      })
//
//      "4mld" must {
//
//        val max: Int = 51
//
//        val is5mldEnabled: Boolean = false
//        val isTaxable: Boolean = true
//
//        "return OK and the correct view for a GET" in {
//
//          val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()
//
//          val request = FakeRequest(GET, addAssetsRoute)
//
//          val view = application.injector.instanceOf[MaxedOutView]
//
//          val result = route(application, request).value
//
//          status(result) mustEqual OK
//
//          val content = contentAsString(result)
//
//          content mustEqual
//            view(Nil, assets(max), "You have added 51 assets", max, "addAssets")(request, messages).toString
//
//          content must include("You cannot add another asset as you have entered a maximum of 51.")
//          content must include("You can add another asset by removing an existing one, or write to HMRC with details of any additional assets.")
//
//          application.stop()
//        }
//
//        "redirect to next page and set AddAssetsPage to NoComplete for a POST" in {
//
//          reset(playbackRepository)
//          when(playbackRepository.set(any())).thenReturn(Future.successful(true))
//          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
//
//          val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()
//
//          val request = FakeRequest(POST, completePostRoute)
//
//          val result = route(application, request).value
//
//          status(result) mustEqual SEE_OTHER
//
//          redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url
//
//          verify(playbackRepository).set(uaCaptor.capture)
//          uaCaptor.getValue.get(AddAssetsPage).get mustBe NoComplete
//
//          application.stop()
//        }
//      }
//
//      "5mld" when {
//
//        "taxable" must {
//
//          val max: Int = 76
//
//          val is5mldEnabled: Boolean = true
//          val isTaxable: Boolean = true
//
//          "return OK and the correct view for a GET" in {
//
//            val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()
//
//            val request = FakeRequest(GET, addAssetsRoute)
//
//            val view = application.injector.instanceOf[MaxedOutView]
//
//            val result = route(application, request).value
//
//            status(result) mustEqual OK
//
//            val content = contentAsString(result)
//
//            content mustEqual
//              view(Nil, assets(max), "You have added 76 assets", max, "addAssets")(request, messages).toString
//
//            content must include("You cannot add another asset as you have entered a maximum of 76.")
//            content must include("You can add another asset by removing an existing one, or write to HMRC with details of any additional assets.")
//
//            application.stop()
//          }
//
//          "redirect to next page and set AddAssetsPage to NoComplete for a POST" in {
//
//            reset(playbackRepository)
//            when(playbackRepository.set(any())).thenReturn(Future.successful(true))
//            val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
//
//            val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()
//
//            val request = FakeRequest(POST, completePostRoute)
//
//            val result = route(application, request).value
//
//            status(result) mustEqual SEE_OTHER
//
//            redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url
//
//            verify(playbackRepository).set(uaCaptor.capture)
//            uaCaptor.getValue.get(AddAssetsPage).get mustBe NoComplete
//
//            application.stop()
//          }
//        }
//
//        "non-taxable" must {
//
//          val max: Int = 25
//
//          val is5mldEnabled: Boolean = true
//          val isTaxable: Boolean = false
//
//          "return OK and the correct view for a GET" in {
//
//            val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()
//
//            val request = FakeRequest(GET, addAssetsRoute)
//
//            val view = application.injector.instanceOf[MaxedOutView]
//
//            val result = route(application, request).value
//
//            status(result) mustEqual OK
//
//            val content = contentAsString(result)
//
//            content mustEqual
//              view(Nil, assets(max), "You have added 25 non-EEA companies", max, "addAssets.nonTaxable")(request, messages).toString
//
//            content must include("You cannot add another non-EEA company as you have entered a maximum of 25.")
//            content must include("You can add another non-EEA company by removing an existing one, or write to HMRC with details of any additional non-EEA companies.")
//
//            application.stop()
//          }
//
//          "redirect to next page and set AddAssetsPage to NoComplete for a POST" in {
//
//            reset(playbackRepository)
//            when(playbackRepository.set(any())).thenReturn(Future.successful(true))
//            val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
//
//            val application = applicationBuilder(userAnswers = Some(userAnswers(max, is5mldEnabled, isTaxable))).build()
//
//            val request = FakeRequest(POST, completePostRoute)
//
//            val result = route(application, request).value
//
//            status(result) mustEqual SEE_OTHER
//
//            redirectLocation(result).value mustEqual fakeNavigator.desiredRoute.url
//
//            verify(playbackRepository).set(uaCaptor.capture)
//            uaCaptor.getValue.get(AddAssetsPage).get mustBe NoComplete
//
//            application.stop()
//          }
//        }
//      }
//    }
  }


  class FakeService(testAssets: Assets) extends TrustService {

    override def getAssets(identifier: String)(implicit hc:HeaderCarrier, ec:ExecutionContext): Future[Assets] =
      Future.successful(testAssets)

    override def getMonetaryAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[AssetMonetaryAmount] =
      Future.successful(testAssets.monetary(index))

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

