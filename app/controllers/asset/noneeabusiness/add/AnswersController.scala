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
                                   navigator: AssetsNavigator,
                                   repository: PlaybackRepository
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      Ok(view(printHelper(userAnswers = request.userAnswers, index = 0, provisional = provisional, name = request.name)))
    }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      mapper(request.userAnswers) match {
        case None =>
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))

        case Some(asset) =>
          connector.getAssets(request.userAnswers.identifier).flatMap { data =>
            val exists = data.nonEEABusiness.exists { ele =>
              ele.orgName.equalsIgnoreCase(asset.orgName) &&
                ele.address.line1.equalsIgnoreCase(asset.address.line1) &&
                ele.govLawCountry.equalsIgnoreCase(asset.govLawCountry) &&
                ele.startDate.equals(asset.startDate) &&
                ele.endDate.equals(asset.endDate)
            }

            val addIfNeededF: Future[Unit] =
              if (!exists) connector.addNonEeaBusinessAsset(request.userAnswers.identifier, asset).map(_ => ())
              else Future.successful(())

            addIfNeededF.flatMap(_ => cleanAllAndRedirect())
          }
      }
    }

  private def cleanAllAndRedirect() (implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.cleanup.fold(
      _ => Future.successful(
        Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
      ),
      cleaned => repository.set(cleaned).map { _ =>
        Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
      }
    )
  }
}