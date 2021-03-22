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

package controllers.asset.noneeabusiness

import controllers.actions._
import models.Status.Completed
import models.requests.RegistrationDataRequest
import pages.AssetStatus
import pages.asset.noneeabusiness.NamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.NonEeaBusinessPrintHelper
import views.html.asset.noneeabusiness.AnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AnswersController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   registrationsRepository: RegistrationsRepository,
                                   identify: RegistrationIdentifierAction,
                                   getData: DraftIdRetrievalActionProvider,
                                   requireData: RegistrationDataRequiredAction,
                                   requiredAnswer: RequiredAnswerActionProvider,
                                   view: AnswersView,
                                   val controllerComponents: MessagesControllerComponents,
                                   printHelper: NonEeaBusinessPrintHelper
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(index: Int): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen
      getData() andThen
      requireData andThen
      requiredAnswer(RequiredAnswer(NamePage(index), routes.NameController.onPageLoad(index)))

  def onPageLoad(index: Int): Action[AnyContent] = actions(index) {
    implicit request =>

      val name = request.userAnswers.get(NamePage(index)).get

      val section = printHelper.checkDetailsSection(
        userAnswers = request.userAnswers,
        arg = name,
        index = index
      )

      Ok(view(index, section))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions(index).async {
    implicit request =>

      val answers = request.userAnswers.set(AssetStatus(index), Completed)

      for {
        updatedAnswers <- Future.fromTry(answers)
        _ <- registrationsRepository.set(updatedAnswers)
      } yield Redirect(controllers.asset.routes.AddAssetsController.onPageLoad())

  }
}
