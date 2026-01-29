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

package controllers.asset.noneeabusiness.add

import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.noneeabusiness.NameRequiredAction
import handlers.ErrorHandler
import mapping.NonEeaBusinessAssetMapper
import models.requests.DataRequest
import navigation.AssetsNavigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.NonEeaBusinessPrintHelper
import viewmodels.AnswerSection
import views.html.asset.noneeabusiness.add.AnswersView
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AnswersController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  nameAction: NameRequiredAction,
  connector: TrustsConnector,
  view: AnswersView,
  val controllerComponents: MessagesControllerComponents,
  printHelper: NonEeaBusinessPrintHelper,
  mapper: NonEeaBusinessAssetMapper,
  errorHandler: ErrorHandler,
  navigator: AssetsNavigator,
  repository: PlaybackRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(index: Int): Action[AnyContent] =
    (standardActionSets.verifiedForIdentifier andThen nameAction) { implicit request =>
      val name: String           = request.name
      val section: AnswerSection =
        printHelper(userAnswers = request.userAnswers, index = index, provisional = provisional, name = name)
      Ok(view(index, section))
    }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async { implicit request =>
    mapper(request.userAnswers) match {
      case None        => errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      case Some(asset) =>
        connector.getAssets(request.userAnswers.identifier).flatMap { data =>
          if (data.nonEEABusiness.nonEmpty && (data.nonEEABusiness.size - 1 == index)) {
            connector.amendNonEeaBusinessAsset(request.userAnswers.identifier, index, asset).flatMap { response =>
              response.status match {
                case OK | NO_CONTENT => cleanAllAndRedirect()
                case _               => errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
              }
            }
          } else {
            val exists = data.nonEEABusiness.exists { e =>
              e.orgName.equalsIgnoreCase(asset.orgName) &&
              e.address.line1.equalsIgnoreCase(asset.address.line1) &&
              e.govLawCountry.equalsIgnoreCase(asset.govLawCountry) &&
              e.startDate == asset.startDate &&
              e.endDate == asset.endDate
            }
            if (!exists) {
              connector.addNonEeaBusinessAsset(request.userAnswers.identifier, asset).flatMap { response =>
                response.status match {
                  case OK | NO_CONTENT => cleanAllAndRedirect()
                  case _               => errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
                }
              }
            } else {
              cleanAllAndRedirect()
            }
          }
        }
    }
  }

  private def cleanAllAndRedirect()(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val next = navigator.redirectToAddAssetPage(request.userAnswers.isMigratingToTaxable)
    request.userAnswers.cleanupPreservingNonEea.fold(
      _ => Future.successful(Redirect(next)),
      ua => repository.set(ua).map(_ => Redirect(next))
    )
  }

}
