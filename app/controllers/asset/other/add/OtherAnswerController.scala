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

import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.property_or_land.NameRequiredAction
import handlers.ErrorHandler
import javax.inject.Inject
import mapping.OtherAssetMapper
import pages.asset.other.OtherAssetDescriptionPage
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
                                       view: OtherAssetAnswersView,
                                       val controllerComponents: MessagesControllerComponents,
                                       printHelper: OtherPrintHelper,
                                       connector: TrustsConnector,
                                       mapper: OtherAssetMapper,
                                       errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>

      val description = request.userAnswers.get(OtherAssetDescriptionPage).get

      val section: AnswerSection = printHelper(userAnswers = request.userAnswers, provisional, description)
      Ok(view(section))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      mapper(request.userAnswers) match {
        case None =>
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
        case Some(asset) =>
          connector.addOtherAsset(request.userAnswers.identifier, asset).map(_ =>
            Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
          )
      }
  }
}
