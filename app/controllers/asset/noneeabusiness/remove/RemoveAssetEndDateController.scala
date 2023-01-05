/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.asset.noneeabusiness.remove

import controllers.actions.StandardActionSets
import forms.EndDateFormProvider
import handlers.ErrorHandler
import models.RemoveAsset
import models.assets.AssetNameType
import navigation.AssetsNavigator
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.noneeabusiness.remove.RemoveAssetEndDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveAssetEndDateController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              standardActionSets: StandardActionSets,
                                              formProvider: EndDateFormProvider,
                                              trustService: TrustService,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: RemoveAssetEndDateView,
                                              errorHandler: ErrorHandler,
                                              navigator: AssetsNavigator
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val messagePrefix: String = "nonEeaBusiness.endDate"

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trustService.getNonEeaBusinessAsset(request.userAnswers.identifier, index).map {
        asset =>
          val form = formProvider.withConfig(messagePrefix, asset.startDate)
          Ok(view(form, index, asset.orgName))
      } recoverWith {
        case iobe: IndexOutOfBoundsException =>
          logger.warn(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" user cannot remove asset as asset was not found ${iobe.getMessage}: IndexOutOfBoundsException")

          Future.successful(Redirect(navigator.redirectToAddAssetPage(request.userAnswers.isMigratingToTaxable)))
        case _ =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]" +
            s" user cannot remove asset as asset was not found")
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trustService.getNonEeaBusinessAsset(request.userAnswers.identifier, index).flatMap{
        asset => {
          val form = formProvider.withConfig(messagePrefix, asset.startDate)
          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(view(formWithErrors, index, asset.orgName))),
            endDate => {
              trustService.removeAsset(request.userAnswers.identifier, RemoveAsset(AssetNameType.NonEeaBusinessAssetNameType, index, endDate)).map(_ =>
                Redirect(navigator.redirectToAddAssetPage(request.userAnswers.isMigratingToTaxable))
              )
            }
          )
        }
      }
  }

}
