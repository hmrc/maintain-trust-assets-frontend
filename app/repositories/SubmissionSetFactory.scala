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

package repositories

import mapping.AssetMapper
import models.Status.Completed
import models.{RegistrationSubmission, Status, UserAnswers}
import pages.RegistrationProgress
import play.api.i18n.Messages
import play.api.libs.json.Json
import utils.answers._
import viewmodels.{AnswerRow, AnswerSection}

import javax.inject.Inject

class SubmissionSetFactory @Inject()(registrationProgress: RegistrationProgress,
                                     assetMapper: AssetMapper,
                                     moneyAnswersHelper: MoneyAnswersHelper,
                                     propertyOrLandAnswersHelper: PropertyOrLandAnswersHelper,
                                     sharesAnswersHelper: SharesAnswersHelper,
                                     businessAnswersHelper: BusinessAnswersHelper,
                                     partnershipAnswersHelper: PartnershipAnswersHelper,
                                     otherAnswersHelper: OtherAnswersHelper,
                                     nonEeaBusinessAnswersHelper: NonEeaBusinessAnswersHelper) {

  def createFrom(userAnswers: UserAnswers)(implicit messages: Messages): RegistrationSubmission.DataSet = {
    val status = registrationProgress.assetsStatus(userAnswers)

    RegistrationSubmission.DataSet(
      Json.toJson(userAnswers),
      status,
      mappedDataIfCompleted(userAnswers, status),
      answerSectionsIfCompleted(userAnswers, status)
    )
  }

  private def mappedDataIfCompleted(userAnswers: UserAnswers, status: Option[Status]): List[RegistrationSubmission.MappedPiece] = {
    if (status.contains(Completed)) {
      assetMapper.build(userAnswers) match {
        case Some(assets) => List(RegistrationSubmission.MappedPiece("trust/assets", Json.toJson(assets)))
        case _ => List.empty
      }
    } else {
      List.empty
    }
  }

  def answerSectionsIfCompleted(userAnswers: UserAnswers, status: Option[Status])
                               (implicit messages: Messages): List[RegistrationSubmission.AnswerSection] = {

    if (status.contains(Completed)) {

      val entitySections: List[AnswerSection] = List(
        moneyAnswersHelper(userAnswers),
        propertyOrLandAnswersHelper(userAnswers),
        sharesAnswersHelper(userAnswers),
        businessAnswersHelper(userAnswers),
        partnershipAnswersHelper(userAnswers),
        otherAnswersHelper(userAnswers),
        nonEeaBusinessAnswersHelper(userAnswers)
      ).flatten

      entitySections match {
        case Nil =>
          List.empty
        case _ =>
          val updatedFirstSection: AnswerSection = AnswerSection(
            entitySections.head.headingKey,
            entitySections.head.rows,
            Some(Messages("answerPage.section.assets.heading"))
          )

          val updatedSections: List[AnswerSection] = updatedFirstSection :: entitySections.tail

          updatedSections.map(convertForSubmission)
      }
    } else {
      List.empty
    }
  }

  private def convertForSubmission(section: AnswerSection): RegistrationSubmission.AnswerSection = {
    RegistrationSubmission.AnswerSection(section.headingKey, section.rows.map(convertForSubmission), section.sectionKey)
  }

  private def convertForSubmission(row: AnswerRow): RegistrationSubmission.AnswerRow = {
    RegistrationSubmission.AnswerRow(row.label, row.answer.toString, row.labelArg)
  }
}
