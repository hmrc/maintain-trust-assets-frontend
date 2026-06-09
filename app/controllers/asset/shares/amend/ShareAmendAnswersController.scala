/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.asset.shares.amend

import config.FrontendAppConfig
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.shares.CompanyNameRequiredAction
import extractors.ShareExtractor
import handlers.ErrorHandler

import javax.inject.Inject
import mapping.ShareAssetMapper
import models.UserAnswers
import models.assets.AssetNameType.SharesAssetNameType
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.SharesPrintHelper
import viewmodels.AnswerSection
import views.html.OutOfBoundsPageNotFoundView
import views.html.asset.shares.amend.ShareAmendAnswersView

import scala.concurrent.{ExecutionContext, Future}

class ShareAmendAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  val controllerComponents: MessagesControllerComponents,
  nameAction: CompanyNameRequiredAction,
  view: ShareAmendAnswersView,
  val outOfBoundsView: OutOfBoundsPageNotFoundView,
  service: TrustService,
  connector: TrustsConnector,
  val appConfig: FrontendAppConfig,
  playbackRepository: PlaybackRepository,
  printHelper: SharesPrintHelper,
  mapper: ShareAssetMapper,
  extractor: ShareExtractor,
  val errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging with IndexAndGenericExceptionRecovery {

  private val provisional: Boolean = false

  private def render(userAnswers: UserAnswers, index: Int, name: String)(implicit
    request: Request[AnyContent]
  ): Result = {
    val section: AnswerSection = printHelper(userAnswers, index, provisional, name)
    Ok(view(section, index))
  }

  def extractAndRender(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      (for {
        shareType       <- service.getSharesAsset(request.userAnswers.identifier, index)
        extractedAnswers = extractor(request.userAnswers, shareType, index)
        extractedF      <- Future.fromTry(extractedAnswers)
        _               <- playbackRepository.set(extractedF)
      } yield render(extractedF, index, shareType.orgName)).recoverWith {
        recoverIndexAndGenericException(SharesAssetNameType, index, request.userAnswers.identifier, "extractAndRender")
      }
  }

  def renderFromUserAnswers(index: Int): Action[AnyContent] =
    standardActionSets.verifiedForIdentifier.andThen(nameAction) { implicit request =>
      render(request.userAnswers, index, request.name)
    }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async { implicit request =>
    mapper(request.userAnswers)
      .map { asset =>
        connector
          .amendSharesAsset(request.userAnswers.identifier, index, asset)
          .map(_ => Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()))
      }
      .getOrElse {
        logger.error(
          s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error mapping user answers to Share Asset $index"
        )
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

}
