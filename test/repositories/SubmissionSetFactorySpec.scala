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

import base.SpecBase
import mapping.AssetMapper
import models.RegistrationSubmission
import models.Status.Completed
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import pages.RegistrationProgress
import pages.asset.TrustOwnsNonEeaBusinessYesNoPage
import play.api.libs.json.Json
import utils.answers._
import viewmodels.AnswerSection

import scala.collection.immutable.Nil

class SubmissionSetFactorySpec extends SpecBase {
  
  "SubmissionSetFactory" when {

    ".createFrom" must {

      "return data set" when {
        "TrustOwnsNonEeaBusinessYesNoPage is false" in {

          val factory = injector.instanceOf[SubmissionSetFactory]
          val answers = emptyUserAnswers.copy(isTaxable = false)
            .set(TrustOwnsNonEeaBusinessYesNoPage, false).success.value
          val result = factory.createFrom(answers)

          result mustBe RegistrationSubmission.DataSet(
            data = Json.toJson(answers),
            status = Some(Completed),
            registrationPieces = Nil,
            answerSections = Nil
          )
        }
      }
    }
    
    ".answerSectionsIfCompleted" must {
      
      "return empty list" when {
        "status is incomplete" in {

          val factory = injector.instanceOf[SubmissionSetFactory]
          val result = factory.answerSectionsIfCompleted(emptyUserAnswers, None)
          
          result mustBe List.empty
        }
      }
      
      "return completed answer sections" when {
        
        val registrationProgress: RegistrationProgress = injector.instanceOf[RegistrationProgress]
        val assetMapper: AssetMapper = injector.instanceOf[AssetMapper]
        
        val moneyAnswersHelper: MoneyAnswersHelper = mock[MoneyAnswersHelper]
        val propertyOrLandAnswersHelper: PropertyOrLandAnswersHelper = mock[PropertyOrLandAnswersHelper]
        val sharesAnswersHelper: SharesAnswersHelper = mock[SharesAnswersHelper]
        val businessAnswersHelper: BusinessAnswersHelper = mock[BusinessAnswersHelper]
        val partnershipAnswersHelper: PartnershipAnswersHelper = mock[PartnershipAnswersHelper]
        val otherAnswersHelper: OtherAnswersHelper = mock[OtherAnswersHelper]
        val nonEeaBusinessAnswersHelper: NonEeaBusinessAnswersHelper = mock[NonEeaBusinessAnswersHelper]

        when(moneyAnswersHelper(any())(any())).thenReturn(Nil)
        when(propertyOrLandAnswersHelper(any())(any())).thenReturn(Nil)
        when(sharesAnswersHelper(any())(any())).thenReturn(Nil)
        when(businessAnswersHelper(any())(any())).thenReturn(Nil)

        val factory = new SubmissionSetFactory(
          registrationProgress = registrationProgress,
          assetMapper = assetMapper,
          moneyAnswersHelper = moneyAnswersHelper,
          propertyOrLandAnswersHelper = propertyOrLandAnswersHelper,
          sharesAnswersHelper = sharesAnswersHelper,
          businessAnswersHelper = businessAnswersHelper,
          partnershipAnswersHelper = partnershipAnswersHelper,
          otherAnswersHelper = otherAnswersHelper,
          nonEeaBusinessAnswersHelper = nonEeaBusinessAnswersHelper
        )

        def assetSection(heading: String): AnswerSection = {
          AnswerSection(
            headingKey = Some(heading),
            rows = Nil,
            sectionKey = None
          )
        }

        "taxable" when {

          val baseAnswers = emptyUserAnswers.copy(isTaxable = true)

          "only one asset" in {

            when(partnershipAnswersHelper(any())(any())).thenReturn(Nil)
            when(otherAnswersHelper(any())(any())).thenReturn(Seq(assetSection("Other 1")))
            when(nonEeaBusinessAnswersHelper(any())(any())).thenReturn(Nil)

            val result = factory.answerSectionsIfCompleted(baseAnswers, Some(Completed))

            result mustBe List(
              RegistrationSubmission.AnswerSection(
                headingKey = Some("Other 1"),
                rows = Nil,
                sectionKey = Some("Assets")
              )
            )
          }

          "when more than one asset" in {

            when(partnershipAnswersHelper(any())(any())).thenReturn(Seq(assetSection("Partnership 1")))
            when(otherAnswersHelper(any())(any())).thenReturn(Seq(assetSection("Other 1")))
            when(nonEeaBusinessAnswersHelper(any())(any())).thenReturn(Nil)

            val result = factory.answerSectionsIfCompleted(baseAnswers, Some(Completed))

            result mustBe List(
              RegistrationSubmission.AnswerSection(
                headingKey = Some("Partnership 1"),
                rows = Nil,
                sectionKey = Some("Assets")
              ),
              RegistrationSubmission.AnswerSection(
                headingKey = Some("Other 1"),
                rows = Nil,
                sectionKey = None
              )
            )
          }
        }

        "non-taxable" when {

          val baseAnswers = emptyUserAnswers.copy(isTaxable = false)

          "only one asset" in {

            when(partnershipAnswersHelper(any())(any())).thenReturn(Nil)
            when(otherAnswersHelper(any())(any())).thenReturn(Nil)
            when(nonEeaBusinessAnswersHelper(any())(any())).thenReturn(Seq(assetSection("Non-EEA Company 1")))

            val result = factory.answerSectionsIfCompleted(baseAnswers, Some(Completed))

            result mustBe List(
              RegistrationSubmission.AnswerSection(
                headingKey = Some("Non-EEA Company 1"),
                rows = Nil,
                sectionKey = Some("Company ownership or controlling interest")
              )
            )
          }

          "when more than one asset" in {

            when(partnershipAnswersHelper(any())(any())).thenReturn(Nil)
            when(otherAnswersHelper(any())(any())).thenReturn(Nil)
            when(nonEeaBusinessAnswersHelper(any())(any())).thenReturn(Seq(assetSection("Non-EEA Company 1"), assetSection("Non-EEA Company 2")))

            val result = factory.answerSectionsIfCompleted(baseAnswers, Some(Completed))

            result mustBe List(
              RegistrationSubmission.AnswerSection(
                headingKey = Some("Non-EEA Company 1"),
                rows = Nil,
                sectionKey = Some("Company ownership or controlling interest")
              ),
              RegistrationSubmission.AnswerSection(
                headingKey = Some("Non-EEA Company 2"),
                rows = Nil,
                sectionKey = None
              )
            )
          }
        }
      }
    }
  }
}
