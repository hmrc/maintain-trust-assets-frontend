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

package controllers.asset.money.remove

import controllers.actions.StandardActionSets
import forms.RemoveIndexFormProvider
import handlers.ErrorHandler
import models.RemoveAsset
import models.assets.AssetNameType
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.TrustService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.CheckAnswersFormatters.currencyFormat
import views.html.asset.money.remove.RemoveAssetYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveAssetYesNoController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            standardActionSets: StandardActionSets,
                                            formProvider: RemoveIndexFormProvider,
                                            trustService: TrustService,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: RemoveAssetYesNoView,
                                            errorHandler: ErrorHandler
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val messagesPrefix: String = "money.removeYesNo"
  private val form = formProvider.apply(messagesPrefix)

  private def redirectToAddAssetsPage(): Result = Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trustService.getMonetaryAsset(request.userAnswers.identifier).map {
        asset =>
          asset.map{ x =>
            Ok(view(form, index, currencyFormat(x.assetMonetaryAmount.toString)))
          }.getOrElse {
            redirectToAddAssetsPage()
          }
      } recoverWith {
        case iobe: IndexOutOfBoundsException =>
          logger.warn(s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]" +
            s" user cannot remove asset as asset was not found ${iobe.getMessage}: IndexOutOfBoundsException")
          Future.successful(redirectToAddAssetsPage())
        case _ =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR/URN: ${request.userAnswers.identifier}]" +
            s" user cannot remove asset as asset was not found")
          errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
      }
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          trustService.getMonetaryAsset(request.userAnswers.identifier).map {
            case Some(asset) =>
              BadRequest(view(formWithErrors, index, currencyFormat(asset.assetMonetaryAmount.toString)))
            case None =>
              redirectToAddAssetsPage()
          }
        },
        value => {
          if (value) {
            removeAsset(request.userAnswers.identifier, index)
          } else {
            Future.successful(redirectToAddAssetsPage())
          }
        }
      )
  }


  private def removeAsset(identifier: String, index: Int)(implicit hc: HeaderCarrier): Future[Result] = {
    trustService.removeAsset(identifier, RemoveAsset(AssetNameType.MoneyAssetNameType, index)).map(_ =>
      redirectToAddAssetsPage()
    )
  }
}
