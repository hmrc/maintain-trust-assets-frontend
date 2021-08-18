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

package controllers.asset.noneeabusiness.add

import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.noneeabusiness.NameRequiredAction
import handlers.ErrorHandler
import mapping.NonEeaBusinessAssetMapper
import navigation.AssetsNavigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.NonEeaBusinessPrintHelper
import viewmodels.AnswerSection
import views.html.asset.noneeabusiness.add.AnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AnswersController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   standardActionSets: StandardActionSets,
                                   nameAction: NameRequiredAction,
                                   connector: TrustsConnector,
                                   view: AnswersView,
                                   val controllerComponents: MessagesControllerComponents,
                                   printHelper: NonEeaBusinessPrintHelper,
                                   mapper: NonEeaBusinessAssetMapper,
                                   errorHandler: ErrorHandler,
                                   navigator: AssetsNavigator
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>

      val isTaxable = request.userAnswers.isTaxable

      val section: AnswerSection = printHelper(userAnswers = request.userAnswers, provisional, request.Name)

      Ok(view(section, isTaxable))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      mapper(request.userAnswers) match {
        case None =>
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
        case Some(asset) =>
          connector.addNonEeaBusinessAsset(request.userAnswers.identifier, asset).map(_ =>
            Redirect(navigator.redirectToAddAssetPage(request.userAnswers.isMigratingToTaxable))
          )
      }
  }
}
