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

package controllers.asset.shares.add

import config.annotations.Shares
import connectors.TrustsConnector
import controllers.actions._
import handlers.ErrorHandler
import javax.inject.Inject
import mapping.ShareAssetMapper
import models.NormalMode
import navigation.Navigator
import pages.asset.shares.add.ShareAnswerPage
import pages.asset.shares.{ShareCompanyNamePage, SharePortfolioNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.Gettable
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.SharesPrintHelper
import viewmodels.AnswerSection
import views.html.asset.shares.add.ShareAnswersView

import scala.concurrent.{ExecutionContext, Future}

class ShareAnswerController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       @Shares navigator: Navigator,
                                       view: ShareAnswersView,
                                       val controllerComponents: MessagesControllerComponents,
                                       printHelper: SharesPrintHelper,
                                       connector: TrustsConnector,
                                       mapper: ShareAssetMapper,
                                       errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

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
      mapper(request.userAnswers) match {
        case None =>
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
        case Some(asset) =>
          connector.addSharesAsset(request.userAnswers.identifier, asset).map(_ =>
            Redirect(navigator.nextPage(ShareAnswerPage, NormalMode, request.userAnswers))
          )
      }
  }
}
