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

package controllers.asset.business.add

import config.annotations.Business
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.business.NameRequiredAction
import handlers.ErrorHandler
import javax.inject.Inject
import mapping.BusinessAssetMapper
import models.NormalMode
import navigation.Navigator
import pages.asset.business.add.BusinessAnswerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.BusinessPrintHelper
import viewmodels.AnswerSection
import views.html.asset.business.add.BusinessAnswersView

import scala.concurrent.{ExecutionContext, Future}

class BusinessAnswersController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           @Business navigator: Navigator,
                                           standardActionSets: StandardActionSets,
                                           nameAction: NameRequiredAction,
                                           view: BusinessAnswersView,
                                           val controllerComponents: MessagesControllerComponents,
                                           printHelper: BusinessPrintHelper,
                                           connector: TrustsConnector,
                                           mapper: BusinessAssetMapper,
                                           errorHandler: ErrorHandler
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      val section: AnswerSection = printHelper(userAnswers = request.userAnswers, provisional, request.Name)
      Ok(view(section))
  }

  def onSubmit(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction).async {
    implicit request =>
      mapper(request.userAnswers) match {
        case None =>
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
        case Some(asset) =>
          connector.addBusinessAsset(request.userAnswers.identifier, asset).map(_ =>
            Redirect(navigator.nextPage(BusinessAnswerPage, NormalMode, request.userAnswers))
          )
      }
  }
}
