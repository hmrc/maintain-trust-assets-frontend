/*
 * Copyright 2026 HM Revenue & Customs
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

package utils.print

import controllers.asset.property_or_land.routes._
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.asset.property_or_land._
import play.api.i18n.Messages
import utils.AnswerRowConverter
import viewmodels.{AnswerRow, AnswerSection}
import javax.inject.Inject

class PropertyOrLandPrintHelper @Inject() (answerRowConverter: AnswerRowConverter) {

  def apply(userAnswers: UserAnswers, index: Int, provisional: Boolean, name: String)(implicit
    messages: Messages
  ): AnswerSection = {

    val bound: answerRowConverter.Bound = answerRowConverter.bind(userAnswers, name)

    def answerRows: Seq[AnswerRow] = {
      val mode: Mode = if (provisional) NormalMode else CheckMode
      Seq(
        bound.assetTypeQuestion(0),
        bound.yesNoQuestion(
          PropertyOrLandAddressYesNoPage(index),
          "propertyOrLand.addressYesNo",
          PropertyOrLandAddressYesNoController.onPageLoad(index, mode).url
        ),
        bound.yesNoQuestion(
          PropertyOrLandAddressUkYesNoPage(index),
          "propertyOrLand.addressUkYesNo",
          PropertyOrLandAddressUkYesNoController.onPageLoad(index, mode).url
        ),
        bound.addressQuestion(
          PropertyOrLandUKAddressPage(index),
          "propertyOrLand.ukAddress",
          PropertyOrLandUKAddressController.onPageLoad(index, mode).url
        ),
        bound.addressQuestion(
          PropertyOrLandInternationalAddressPage(index),
          "propertyOrLand.internationalAddress",
          PropertyOrLandInternationalAddressController.onPageLoad(index, mode).url
        ),
        bound.stringQuestion(
          PropertyOrLandDescriptionPage(index),
          "propertyOrLand.description",
          PropertyOrLandDescriptionController.onPageLoad(index, mode).url
        ),
        bound.currencyQuestion(
          PropertyOrLandTotalValuePage(index),
          "propertyOrLand.totalValue",
          PropertyOrLandTotalValueController.onPageLoad(index, mode).url
        ),
        bound.yesNoQuestion(
          TrustOwnAllThePropertyOrLandPage(index),
          "propertyOrLand.trustOwnAllYesNo",
          TrustOwnAllThePropertyOrLandController.onPageLoad(index, mode).url
        ),
        bound.currencyQuestion(
          PropertyLandValueTrustPage(index),
          "propertyOrLand.valueInTrust",
          PropertyLandValueTrustController.onPageLoad(index, mode).url
        )
      ).flatten
    }

    AnswerSection(headingKey = None, rows = answerRows)

  }

}
