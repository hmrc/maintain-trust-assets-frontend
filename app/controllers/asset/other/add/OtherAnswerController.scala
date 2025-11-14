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

package controllers.asset.other.add

import config.annotations.Other
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.property_or_land.NameRequiredAction
import handlers.ErrorHandler
import mapping.OtherAssetMapper
import models.NormalMode
import models.requests.DataRequest
import navigation.Navigator
import pages.asset.other.OtherAssetDescriptionPage
import pages.asset.other.add.OtherAnswerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.OtherPrintHelper
import viewmodels.AnswerSection
import views.html.asset.other.add.OtherAssetAnswersView
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherAnswerController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       nameAction: NameRequiredAction,
                                       connector: TrustsConnector,
                                       @Other navigator: Navigator,
                                       view: OtherAssetAnswersView,
                                       val controllerComponents: MessagesControllerComponents,
                                       printHelper: OtherPrintHelper,
                                       mapper: OtherAssetMapper,
                                       errorHandler: ErrorHandler,
                                       repository: PlaybackRepository
                                     )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(index: Int): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      val name: String = request.userAnswers.get(OtherAssetDescriptionPage(index)).getOrElse("")
      val section: AnswerSection = printHelper(userAnswers = request.userAnswers, index = index, provisional = provisional, name = name)
      Ok(view(index, section))
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      mapper(request.userAnswers) match {
        case None => errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
        case Some(asset) =>
          connector.getAssets(request.userAnswers.identifier).flatMap { data =>
            if (data.other.nonEmpty && (data.other.size - 1 == index)) {
              connector.amendOtherAsset(request.userAnswers.identifier, index, asset).flatMap { response =>
                response.status match {
                  case OK | NO_CONTENT => cleanAllAndRedirect(index)
                  case _               => errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
                }
              }
            } else {
              val exists = data.other.exists(e => e.description.equalsIgnoreCase(asset.description) && e.value == asset.value)
              if (!exists) {
                connector.addOtherAsset(request.userAnswers.identifier, asset).flatMap { response =>
                  response.status match {
                    case OK | NO_CONTENT => cleanAllAndRedirect(index)
                    case _               => errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
                  }
                }
              } else {
                cleanAllAndRedirect(index)
              }
            }
          }
      }
  }

  private def cleanAllAndRedirect(index: Int)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val next = navigator.nextPage(OtherAnswerPage(index), NormalMode, request.userAnswers)
    request.userAnswers.cleanupPreservingOther.fold(
      _ => Future.successful(Redirect(next)),
      ua => repository.set(ua).map(_ => Redirect(next))
    )
  }
}
