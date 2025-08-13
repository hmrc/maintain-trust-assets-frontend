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

package controllers.asset.property_or_land.add

import config.annotations.PropertyOrLand
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.property_or_land.NameRequiredAction
import handlers.ErrorHandler

import javax.inject.Inject
import mapping.PropertyOrLandMapper
import models.NormalMode
import navigation.Navigator
import pages.asset.property_or_land.add.PropertyOrLandAnswerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.PropertyOrLandPrintHelper
import viewmodels.AnswerSection
import views.html.asset.property_or_land.add.PropertyOrLandAnswersView

import scala.concurrent.{ExecutionContext, Future}

class PropertyOrLandAnswerController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                @PropertyOrLand navigator: Navigator,
                                                standardActionSets: StandardActionSets,
                                                nameAction: NameRequiredAction,
                                                view: PropertyOrLandAnswersView,
                                                val controllerComponents: MessagesControllerComponents,
                                                printHelper: PropertyOrLandPrintHelper,
                                                connector: TrustsConnector,
                                                mapper: PropertyOrLandMapper,
                                                errorHandler: ErrorHandler
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
        case None =>
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
        case Some(asset) =>
          connector.amendPropertyOrLandAsset(request.userAnswers.identifier, index, asset).flatMap { response =>
            response.status match {
              case OK | NO_CONTENT =>
                Future.successful(
                  Redirect(navigator.nextPage(PropertyOrLandAnswerPage(index), NormalMode, request.userAnswers))
                )

              case _ =>
                connector.getAssets(request.userAnswers.identifier).flatMap { data =>
                  val matchFound = data.propertyOrLand.exists(existing =>
                    existing.name.equalsIgnoreCase(asset.name) &&
                      existing.address == asset.address &&
                      existing.buildingLandName == asset.buildingLandName &&
                      existing.descriptionOrAddress == asset.descriptionOrAddress &&
                      existing.valueFull == asset.valueFull &&
                      existing.valuePrevious == asset.valuePrevious
                  )

                  if (!matchFound) {
                    connector.addPropertyOrLandAsset(request.userAnswers.identifier, asset).map { _ =>
                      Redirect(navigator.nextPage(PropertyOrLandAnswerPage(index), NormalMode, request.userAnswers))
                    }
                  } else {
                    Future.successful(
                      Redirect(navigator.nextPage(PropertyOrLandAnswerPage(index), NormalMode, request.userAnswers))
                    )
                  }
                }
            }
          }
      }
  }

}
