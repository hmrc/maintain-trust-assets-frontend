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

package controllers.asset.shares

import config.annotations.Shares
import controllers.actions._
import models.Status.Completed
import navigation.Navigator
import pages.AssetStatus
import pages.asset.shares.{ShareAnswerPage, ShareCompanyNamePage, SharePortfolioNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.Gettable
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.SharesPrintHelper
import views.html.asset.shares.ShareAnswersView
import javax.inject.Inject
import models.NormalMode
import viewmodels.AnswerSection

import scala.concurrent.{ExecutionContext, Future}

class ShareAnswerController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       repository: PlaybackRepository,
                                       @Shares navigator: Navigator,
                                       view: ShareAnswersView,
                                       val controllerComponents: MessagesControllerComponents,
                                       printHelper: SharesPrintHelper
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

//  private def actions(index: Int): ActionBuilder[RegistrationDataRequest, AnyContent] =
//    identify andThen
//      getData() andThen
//      requireData andThen
//      requiredAnswer(RequiredAnswer(SharesInAPortfolioPage, routes.SharesInAPortfolioController.onPageLoad(index)))
// TODO

  private val provisional: Boolean = true

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>

      def getPage(page: Gettable[String]): Option[String] = {
        request.userAnswers.get(page)
      }

      val name: String = (getPage(ShareCompanyNamePage), getPage(SharePortfolioNamePage)) match {
        case (Some(name), None) => name
        case (None, Some(name)) => name
        case _ => request.messages(messagesApi)("assets.defaultText")
      }

      val section: AnswerSection = printHelper(userAnswers = request.userAnswers, provisional, name)

      Ok(view(section))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      val answers = request.userAnswers.set(AssetStatus, Completed)

      for {
        updatedAnswers <- Future.fromTry(answers)
        _ <- repository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(ShareAnswerPage, NormalMode, request.userAnswers))

  }
}
