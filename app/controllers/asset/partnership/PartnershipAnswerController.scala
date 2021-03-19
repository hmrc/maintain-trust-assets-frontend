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

import config.annotations.Partnership
import controllers.actions._
import models.Status.Completed
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.AssetStatus
import pages.asset.partnership._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.PartnershipPrintHelper
import views.html.asset.partnership.PartnershipAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipAnswerController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             repository: RegistrationsRepository,
                                             @Partnership navigator: Navigator,
                                             identify: RegistrationIdentifierAction,
                                             getData: DraftIdRetrievalActionProvider,
                                             requireData: RegistrationDataRequiredAction,
                                             requiredAnswer: RequiredAnswerActionProvider,
                                             view: PartnershipAnswersView,
                                             val controllerComponents: MessagesControllerComponents,
                                             printHelper: PartnershipPrintHelper
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(index: Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen
      getData(draftId) andThen
      requireData andThen
      requiredAnswer(RequiredAnswer(PartnershipDescriptionPage(index), routes.PartnershipDescriptionController.onPageLoad(index, draftId))) andThen
      requiredAnswer(RequiredAnswer(PartnershipStartDatePage(index), routes.PartnershipStartDateController.onPageLoad(index, draftId)))

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val sections = printHelper.checkDetailsSection(
        userAnswers = request.userAnswers,
        index = index,
        draftId = draftId
      )

      Ok(view(index, draftId, sections))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      val answers = request.userAnswers.set(AssetStatus(index), Completed)

      for {
        updatedAnswers <- Future.fromTry(answers)
        _ <- repository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(PartnershipAnswerPage, draftId)(request.userAnswers))

  }
}
