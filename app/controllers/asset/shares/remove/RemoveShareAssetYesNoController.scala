/*
 * Copyright 2026 HM Revenue & Customs
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

package controllers.asset.shares.remove

import controllers.actions.{IndexAndGenericExceptionRecovery, StandardActionSets}
import forms.RemoveIndexFormProvider
import handlers.ErrorHandler

import javax.inject.Inject
import models.RemoveAsset
import models.assets.AssetNameType
import models.assets.AssetNameType.SharesAssetNameType
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.OutOfBoundsPageNotFoundView
import views.html.asset.shares.remove.RemoveShareAssetYesNoView

import scala.concurrent.{ExecutionContext, Future}

class RemoveShareAssetYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  formProvider: RemoveIndexFormProvider,
  trustService: TrustService,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveShareAssetYesNoView,
  val outOfBoundsView: OutOfBoundsPageNotFoundView,
  val errorHandler: ErrorHandler
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport with Logging with IndexAndGenericExceptionRecovery {

  private val messagesPrefix: String = "shares.removeYesNo"
  private val form                   = formProvider.apply(messagesPrefix)

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async { implicit request =>
    trustService
      .getSharesAsset(request.userAnswers.identifier, index)
      .map { asset =>
        Ok(view(form, index, asset.orgName))
      }
      .recoverWith {
        recoverIndexAndGenericException(SharesAssetNameType, index, request.userAnswers.identifier, "onPageLoad")
      }
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[_]) =>
          trustService.getSharesAsset(request.userAnswers.identifier, index).map { asset =>
            BadRequest(view(formWithErrors, index, asset.orgName))
          },
        value =>
          if (value) {
            trustService.getSharesAsset(request.userAnswers.identifier, index).flatMap { _ =>
              trustService
                .removeAsset(request.userAnswers.identifier, RemoveAsset(AssetNameType.SharesAssetNameType, index))
                .map(_ => Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()))
            }
          } else {
            Future.successful(Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()))
          }
      )
      .recoverWith {
        recoverIndexAndGenericException(SharesAssetNameType, index, request.userAnswers.identifier, "onSubmit")
      }
  }

}
