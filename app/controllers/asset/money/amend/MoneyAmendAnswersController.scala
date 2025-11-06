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

package controllers.asset.money.amend

import config.FrontendAppConfig
import connectors.TrustsConnector
import controllers.actions._
import extractors.MoneyAssetExtractor
import handlers.ErrorHandler
import mapping.MoneyAssetMapper
import models.UserAnswers
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.MoneyPrintHelper
import viewmodels.AnswerSection
import views.html.asset.money.amend.MoneyAmendAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MoneyAmendAnswersController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   standardActionSets: StandardActionSets,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: MoneyAmendAnswersView,
                                   service: TrustService,
                                   connector: TrustsConnector,
                                   val appConfig: FrontendAppConfig,
                                   playbackRepository: PlaybackRepository,
                                   printHelper: MoneyPrintHelper,
                                   mapper: MoneyAssetMapper,
                                   extractor: MoneyAssetExtractor,
                                   errorHandler: ErrorHandler
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val provisional: Boolean = false

  private def render(userAnswers: UserAnswers, index: Int, name: String)
                    (implicit request: Request[AnyContent]): Result = {
    val section: AnswerSection = printHelper(userAnswers, index, provisional, name)
    Ok(view(section, index))
  }

  def extractAndRender(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      service.getMonetaryAsset(request.userAnswers.identifier, index) flatMap {
        moneyAsset =>
          val extractedAnswers = extractor(request.userAnswers, moneyAsset, index)
          for {
            extractedF <- Future.fromTry(extractedAnswers)
            _ <- playbackRepository.set(extractedF)
          } yield {
            render(extractedF, index, moneyAsset.assetMonetaryAmount.toString)
          }
      } recoverWith {
        case e =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" error showing the user the check answers for Other Asset $index ${e.getMessage}")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  def renderFromUserAnswers(index: Int) : Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>
      render(request.userAnswers, index, "")
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      mapper(request.userAnswers).map {
        asset =>
          connector.amendMoneyAsset(request.userAnswers.identifier, index, asset).map(_ =>
            Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
          )
      }.getOrElse {
        logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
          s" error mapping user answers to Other Asset $index")
        errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }
}
