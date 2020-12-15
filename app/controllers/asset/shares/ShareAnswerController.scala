/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.asset.shares

import config.annotations.Shares
import controllers.actions._
import models.NormalMode
import models.Status.Completed
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.AssetStatus
import pages.asset.shares.{ShareAnswerPage, ShareCompanyNamePage, SharePortfolioNamePage, SharesInAPortfolioPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import queries.Gettable
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.print.SharesPrintHelper
import views.html.asset.shares.ShareAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ShareAnswerController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       repository: RegistrationsRepository,
                                       @Shares navigator: Navigator,
                                       identify: RegistrationIdentifierAction,
                                       getData: DraftIdRetrievalActionProvider,
                                       requireData: RegistrationDataRequiredAction,
                                       requiredAnswer: RequiredAnswerActionProvider,
                                       view: ShareAnswersView,
                                       val controllerComponents: MessagesControllerComponents,
                                       printHelper: SharesPrintHelper
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(index: Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen
      getData(draftId) andThen
      requireData andThen
      requiredAnswer(RequiredAnswer(SharesInAPortfolioPage(index), routes.SharesInAPortfolioController.onPageLoad(NormalMode, index, draftId)))

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      def getPage(page: Gettable[String]): Option[String] = {
        request.userAnswers.get(page)
      }

      val name: String = (getPage(ShareCompanyNamePage(index)), getPage(SharePortfolioNamePage(index))) match {
        case (Some(name), None) => name
        case (None, Some(name)) => name
        case _ => "" // TODO - redirect to start of journey
      }

      val sections = printHelper.checkDetailsSection(
        userAnswers = request.userAnswers,
        arg = name,
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
      } yield Redirect(navigator.nextPage(ShareAnswerPage, NormalMode, draftId)(request.userAnswers))

  }
}
