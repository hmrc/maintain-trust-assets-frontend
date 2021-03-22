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

package controllers.asset.partnership

import base.SpecBase
import models.Status.Completed
import models.WhatKindOfAsset.Partnership
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.AssetStatus
import pages.asset.WhatKindOfAssetPage
import pages.asset.partnership._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.print.PartnershipPrintHelper
import views.html.asset.partnership.PartnershipAnswersView

import java.time.{LocalDate, ZoneOffset}

class PartnershipAnswerControllerSpec extends SpecBase {

  val index: Int = 0
  val validDate: LocalDate = LocalDate.now(ZoneOffset.UTC)

  lazy val partnershipAnswerRoute: String = routes.PartnershipAnswerController.onPageLoad(index).url

  "PartnershipAnswer Controller" must {

      "return OK and the correct view for a GET" in {

        val userAnswers =
          emptyUserAnswers
            .set(WhatKindOfAssetPage(index), Partnership).success.value
            .set(PartnershipDescriptionPage(index), "Partnership Description").success.value
            .set(PartnershipStartDatePage(index), validDate).success.value
            .set(AssetStatus(index), Completed).success.value

        val expectedSections = Nil
        val mockPrintHelper: PartnershipPrintHelper = mock[PartnershipPrintHelper]
        when(mockPrintHelper.checkDetailsSection(any(), any(), any())(any())).thenReturn(Nil)

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[PartnershipPrintHelper].toInstance(mockPrintHelper))
          .build()

        val request = FakeRequest(GET, partnershipAnswerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnershipAnswersView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(index, expectedSections)(fakeRequest, messages).toString

        application.stop()
      }


      "redirect to PartnershipDescription page on a GET if no answer for 'What is the description for the partnership?' at index" in {

        val answers =
          emptyUserAnswers
            .set(WhatKindOfAssetPage(index), Partnership).success.value
            .set(PartnershipStartDatePage(index), validDate).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        val request = FakeRequest(GET, partnershipAnswerRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.PartnershipDescriptionController.onPageLoad(index).url

        application.stop()

      }

    "redirect to PartnershipStartDate page on a GET if no answer for 'When did the partnership start?' at index" in {

      val answers =
        emptyUserAnswers
          .set(WhatKindOfAssetPage(index), Partnership).success.value
          .set(PartnershipDescriptionPage(index), "Partnership Description").success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      val request = FakeRequest(GET, partnershipAnswerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.PartnershipStartDateController.onPageLoad(index).url

      application.stop()

    }

    "redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, partnershipAnswerRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

  }
}
