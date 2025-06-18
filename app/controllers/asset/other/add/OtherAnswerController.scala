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

package controllers.asset.other.add

import config.annotations.Other
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.property_or_land.NameRequiredAction
import handlers.ErrorHandler

import javax.inject.Inject
import mapping.OtherAssetMapper
import models.NormalMode
import navigation.Navigator
import pages.asset.other.OtherAssetDescriptionPage
import pages.asset.other.add.OtherAnswerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.OtherPrintHelper
import viewmodels.AnswerSection
import views.html.asset.other.add.OtherAssetAnswersView

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
                                       errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(index: Int): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      val description = request.userAnswers.get(OtherAssetDescriptionPage(index)).getOrElse("")
      val section: AnswerSection = printHelper(userAnswers = request.userAnswers, index, provisional, description)
      Ok(view(index, section))
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      mapper(request.userAnswers) match {
        case None =>
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))

        case Some(asset) =>
          connector.amendOtherAsset(request.userAnswers.identifier, index, asset).flatMap { response =>
            response.status match {
              case OK | NO_CONTENT =>
                Future.successful(
                  Redirect(navigator.nextPage(OtherAnswerPage(index + 1), NormalMode, request.userAnswers))
                )

              case _ =>
                connector.getAssets(request.userAnswers.identifier).flatMap { data =>
                  val matchFound = data.other.exists(existing =>
                    existing.description.equalsIgnoreCase(asset.description) &&
                      existing.value == asset.value
                  )

                  if (!matchFound) {
                    connector.addOtherAsset(index, request.userAnswers.identifier, asset).map { _ =>
                      Redirect(navigator.nextPage(OtherAnswerPage(index + 1), NormalMode, request.userAnswers))
                    }
                  } else {
                    Future.successful(
                      Redirect(navigator.nextPage(OtherAnswerPage(index + 1), NormalMode, request.userAnswers))
                    )
                  }
                }
            }
          }
      }
  }

}