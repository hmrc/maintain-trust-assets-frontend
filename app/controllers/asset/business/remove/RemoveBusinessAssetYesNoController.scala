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

package controllers.asset.business.remove

import controllers.actions.StandardActionSets
import forms.RemoveIndexFormProvider
import handlers.ErrorHandler
import javax.inject.Inject
import models.RemoveAsset
import models.assets.AssetNameType
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.business.remove.RemoveBusinessAssetYesNoView

import scala.concurrent.{ExecutionContext, Future}

class RemoveBusinessAssetYesNoController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            standardActionSets: StandardActionSets,
                                            formProvider: RemoveIndexFormProvider,
                                            trustService: TrustService,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: RemoveBusinessAssetYesNoView,
                                            errorHandler: ErrorHandler
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val messagesPrefix: String = "business.removeYesNo"
  private val form = formProvider.apply(messagesPrefix)

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trustService.getBusinessAsset(request.userAnswers.identifier, index).map {
        asset =>
          Ok(view(form, index, asset.orgName))
      } recoverWith {
        case iobe: IndexOutOfBoundsException =>
          logger.warn(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" user cannot remove asset as asset was not found ${iobe.getMessage}: IndexOutOfBoundsException")
          Future.successful(Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()))
        case _ =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]" +
            s" user cannot remove asset as asset was not found")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          trustService.getBusinessAsset(request.userAnswers.identifier, index).map {
            asset =>
              BadRequest(view(formWithErrors, index, asset.orgName))
          }
        },
        value => {
          if (value) {
            trustService.getBusinessAsset(request.userAnswers.identifier, index).flatMap {
              _ =>
                trustService.removeAsset(request.userAnswers.identifier, RemoveAsset(AssetNameType.BusinessAssetNameType, index)).map(_ =>
                  Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
                )
            }
          } else {
            Future.successful(Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()))
          }
        }
      )
  }
}
