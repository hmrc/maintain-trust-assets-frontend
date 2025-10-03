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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.MoneyPrintHelper
import viewmodels.AnswerSection
import views.html.asset.money.MoneyAnswersView
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import models.requests.DataRequest
import repositories.PlaybackRepository

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
                                       printHelper: MoneyPrintHelper,
                                       repository: PlaybackRepository
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(index: Int): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      val section: AnswerSection = printHelper(userAnswers = request.userAnswers, index = index, provisional = provisional, name = request.name)
      Ok(view(index, section))
    }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      mapper(request.userAnswers) match {
        case None => errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
        case Some(asset) =>
          connector.getAssets(request.userAnswers.identifier).flatMap { data =>
            if (data.monetary.nonEmpty && (data.monetary.size - 1 == index)) {
              connector.amendMoneyAsset(request.userAnswers.identifier, index, asset).flatMap { response =>
                response.status match {
                  case OK | NO_CONTENT => cleanAllAndRedirect()
                }
              }
            } else {
              val exists = data.monetary.exists(e => e.assetMonetaryAmount == asset.assetMonetaryAmount)
              if (!exists) connector.addMoneyAsset(request.userAnswers.identifier, asset).flatMap(_ => cleanAllAndRedirect())
              else cleanAllAndRedirect()
            }
          }
      }
  }

  private def cleanAllAndRedirect()(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val next = controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()
    request.userAnswers.cleanupPreservingMoney.fold(
      _ => Future.successful(Redirect(next)),
      cleaned => repository.set(cleaned).map(_ => Redirect(next))
    )
  }
}
