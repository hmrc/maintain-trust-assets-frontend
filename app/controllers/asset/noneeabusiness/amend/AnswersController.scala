/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.asset.noneeabusiness.amend

import config.FrontendAppConfig
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.noneeabusiness.NameRequiredAction
import extractors.NonEeaBusinessExtractor
import handlers.ErrorHandler
import mapping.NonEeaBusinessAssetMapper
import models.UserAnswers
import navigation.AssetsNavigator
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.NonEeaBusinessPrintHelper
import views.html.asset.noneeabusiness.amend.AnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AnswersController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   standardActionSets: StandardActionSets,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: AnswersView,
                                   service: TrustService,
                                   connector: TrustsConnector,
                                   val appConfig: FrontendAppConfig,
                                   playbackRepository: PlaybackRepository,
                                   printHelper: NonEeaBusinessPrintHelper,
                                   mapper: NonEeaBusinessAssetMapper,
                                   nameAction: NameRequiredAction,
                                   extractor: NonEeaBusinessExtractor,
                                   errorHandler: ErrorHandler,
                                   navigator: AssetsNavigator
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val provisional: Boolean = false

  private def render(userAnswers: UserAnswers,
                     index: Int,
                     name: String)
                    (implicit request: Request[AnyContent]): Result = {

    Ok(view(
      answerSections = printHelper(userAnswers, provisional, name),
      index = index
    ))
  }

  def extractAndRender(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      service.getNonEeaBusinessAsset(request.userAnswers.identifier, index) flatMap {
        nonEeaBusiness =>
          val extractedAnswers = extractor(request.userAnswers, nonEeaBusiness, index)
          for {
            extractedF <- Future.fromTry(extractedAnswers)
            _ <- playbackRepository.set(extractedF)
          } yield {
            render(extractedF, index, nonEeaBusiness.orgName)
          }
      } recoverWith {
        case e =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error showing the user the check answers for NonEeaBusiness Asset $index ${e.getMessage}")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  def renderFromUserAnswers(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.andThen(nameAction) {
    implicit request =>
      render(request.userAnswers, index, request.name)
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      mapper(request.userAnswers).map {
        asset =>
          connector.amendNonEeaBusinessAsset(request.userAnswers.identifier, index, asset).map(_ =>
            Redirect(navigator.redirectToAddAssetPage(request.userAnswers.isMigratingToTaxable))
          )
      }.getOrElse {
        logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
          s" error mapping user answers to NonEeaBusiness Asset $index, isNew: $provisional")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }
}
