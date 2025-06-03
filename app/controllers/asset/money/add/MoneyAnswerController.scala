/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.asset.money.add

import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.money.NameRequiredAction
import handlers.ErrorHandler
import mapping.MoneyAssetMapper
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.MoneyPrintHelper
import viewmodels.AnswerSection
import views.html.asset.money.MoneyAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MoneyAnswerController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       nameAction: NameRequiredAction,
                                       connector: TrustsConnector,
                                       service: TrustService,
                                       view: MoneyAnswersView,
                                       val controllerComponents: MessagesControllerComponents,
                                       errorHandler: ErrorHandler,
                                       mapper: MoneyAssetMapper,
                                       printHelper: MoneyPrintHelper
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      val section: AnswerSection = printHelper(userAnswers = request.userAnswers, provisional, request.name)
      Ok(view(section))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      mapper(request.userAnswers) match {
        case None =>
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))

        case Some(asset) =>
          service.getMonetaryAsset(request.userAnswers.identifier).flatMap {
            case Some(_) =>
              connector.amendMoneyAsset(request.userAnswers.identifier, 0, asset).map(_ =>
                Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
              )

            case None =>
              connector.addMoneyAsset(request.userAnswers.identifier, asset).map(_ =>
                Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
              )
          }
      }
  }
}