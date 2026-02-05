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

package controllers.asset.partnership.amend

import config.FrontendAppConfig
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.partnership.NameRequiredAction
import extractors.PartnershipAssetExtractor
import handlers.ErrorHandler
import mapping.PartnershipAssetMapper
import models.UserAnswers
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.PartnershipPrintHelper
import viewmodels.AnswerSection
import views.html.asset.partnership.PartnershipAmendAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipAmendAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  val controllerComponents: MessagesControllerComponents,
  view: PartnershipAmendAnswersView,
  service: TrustService,
  connector: TrustsConnector,
  val appConfig: FrontendAppConfig,
  playbackRepository: PlaybackRepository,
  printHelper: PartnershipPrintHelper,
  mapper: PartnershipAssetMapper,
  nameAction: NameRequiredAction,
  extractor: PartnershipAssetExtractor,
  errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging {

  private val provisional: Boolean = false

  private def render(userAnswers: UserAnswers, index: Int, name: String)(implicit
    request: Request[AnyContent]
  ): Result = {
    val section: AnswerSection = printHelper(userAnswers, index, provisional, name)
    Ok(view(section, index))
  }

  def extractAndRender(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      service.getPartnershipAsset(request.userAnswers.identifier, index) flatMap { partnershipType =>
        val extractedAnswers = extractor(request.userAnswers, partnershipType, index)
        for {
          extractedF <- Future.fromTry(extractedAnswers)
          _          <- playbackRepository.set(extractedF)
        } yield render(extractedF, index, partnershipType.description)
      } recoverWith { case e =>
        logger.error(
          s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error showing the user the check answers for Partnership Asset $index ${e.getMessage}"
        )
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  def renderFromUserAnswers(index: Int): Action[AnyContent] =
    standardActionSets.verifiedForIdentifier.andThen(nameAction) { implicit request =>
      render(request.userAnswers, index, request.name)
    }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async { implicit request =>
    mapper(request.userAnswers).map { asset =>
      connector
        .amendPartnershipAsset(request.userAnswers.identifier, index, asset)
        .map(_ => Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()))
    } getOrElse {
      logger.error(
        s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
          s" error mapping user answers to Partnership Asset $index"
      )
      errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
    }
  }

}
