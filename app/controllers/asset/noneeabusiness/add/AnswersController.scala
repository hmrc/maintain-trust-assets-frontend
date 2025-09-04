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

import config.annotations.NonEeaBusiness
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.noneeabusiness.NameRequiredAction
import handlers.ErrorHandler
import mapping.NonEeaBusinessAssetMapper
import models.NormalMode
import navigation.Navigator
import pages.asset.noneeabusiness.add.NonEeaBusinessAnswerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.NonEeaBusinessPrintHelper
import viewmodels.AnswerSection
import views.html.asset.noneeabusiness.add.AnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AnswersController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   standardActionSets: StandardActionSets,
                                   nameAction: NameRequiredAction,
                                   connector: TrustsConnector,
                                   @NonEeaBusiness navigator: Navigator,
                                   view: AnswersView,
                                   val controllerComponents: MessagesControllerComponents,
                                   printHelper: NonEeaBusinessPrintHelper,
                                   mapper: NonEeaBusinessAssetMapper,
                                   errorHandler: ErrorHandler
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(index: Int): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      val section: AnswerSection = printHelper(request.userAnswers, index, provisional, request.name)
      Ok(view(index, section))
    }

  def onSubmit(index: Int): Action[AnyContent] =
    standardActionSets.verifiedForIdentifier.async { implicit request =>
      mapper(request.userAnswers) match {
        case None =>
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))

        case Some(asset) =>
          connector.amendNonEeaBusinessAsset(request.userAnswers.identifier, index, asset).flatMap { response =>
            response.status match {
              case OK | NO_CONTENT =>
                Future.successful(
                  Redirect(navigator.nextPage(NonEeaBusinessAnswerPage(index), NormalMode, request.userAnswers))
                )

              case _ =>
                connector.getAssets(request.userAnswers.identifier).flatMap { data =>
                  val matchFound = data.nonEEABusiness.exists(ele =>
                    ele.orgName.equalsIgnoreCase(asset.orgName) &&
                      ele.address.line1.equalsIgnoreCase(asset.address.line1) &&
                      ele.govLawCountry.equalsIgnoreCase(asset.govLawCountry) &&
                      ele.startDate.equals(asset.startDate) &&
                      ele.endDate.equals(asset.endDate)
                  )

                  if (matchFound) {
                    Future.successful(
                      Redirect(navigator.nextPage(NonEeaBusinessAnswerPage(index), NormalMode, request.userAnswers))
                    )
                  } else {
                    connector.addNonEeaBusinessAsset(request.userAnswers.identifier, asset).map { _ =>
                      Redirect(navigator.nextPage(NonEeaBusinessAnswerPage(index), NormalMode, request.userAnswers))
                    }
                  }
                }
            }
          }
      }
    }
}
