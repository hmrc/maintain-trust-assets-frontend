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

package controllers.asset.partnership.add

import config.annotations.Partnership
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.partnership.NameRequiredAction
import handlers.ErrorHandler
import mapping.PartnershipAssetMapper
import models.NormalMode
import models.requests.DataRequest
import navigation.Navigator
import pages.asset.partnership.add.PartnershipAnswerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.PartnershipPrintHelper
import viewmodels.AnswerSection
import views.html.asset.partnership.PartnershipAnswersView
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipAnswerController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             standardActionSets: StandardActionSets,
                                             nameAction: NameRequiredAction,
                                             connector: TrustsConnector,
                                             @Partnership navigator: Navigator,
                                             view: PartnershipAnswersView,
                                             val controllerComponents: MessagesControllerComponents,
                                             errorHandler: ErrorHandler,
                                             mapper: PartnershipAssetMapper,
                                             printHelper: PartnershipPrintHelper,
                                             repository: PlaybackRepository
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(index: Int): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      val section: AnswerSection = printHelper(request.userAnswers, index, provisional, request.name)
      Ok(view(index, section))
    }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      mapper(request.userAnswers) match {
        case None => errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
        case Some(asset) =>
          connector.getAssets(request.userAnswers.identifier).flatMap { data =>
            if (data.partnerShip.nonEmpty && (data.partnerShip.size - 1 == index)) {
              connector.amendPartnershipAsset(request.userAnswers.identifier, index, asset).flatMap { response =>
                response.status match {
                  case OK | NO_CONTENT => cleanAllAndRedirect(index)
                }
              }
            } else {
              val exists = data.partnerShip.exists(ele =>
                ele.description.equalsIgnoreCase(asset.description) &&
                  ele.partnershipStart == asset.partnershipStart
              )
              if (!exists) connector.addPartnershipAsset(request.userAnswers.identifier, asset).flatMap(_ => cleanAllAndRedirect(index))
              else cleanAllAndRedirect(index)
            }
          }
      }
  }

  private def cleanAllAndRedirect(index: Int)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val next = navigator.nextPage(PartnershipAnswerPage(index), NormalMode, request.userAnswers)
    request.userAnswers.cleanupPreservingPartnership.fold(
      _ => Future.successful(Redirect(next)),
      ua => repository.set(ua).map(_ => Redirect(next))
    )
  }
}
