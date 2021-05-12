/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.asset.nonTaxableToTaxable

import config.FrontendAppConfig
import config.annotations.Assets
import connectors.TrustsStoreConnector
import controllers.actions.StandardActionSets
import forms.{AddAssetsFormProvider, YesNoFormProvider}
import handlers.ErrorHandler
import javax.inject.Inject
import models.Constants._
import models.{AddAssets, NormalMode, UserAnswers}
import navigation.Navigator
import pages.asset.{AddAnAssetYesNoPage, AddAssetsPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi, MessagesProvider}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AddAssetViewHelper
import views.html.asset.noneeabusiness.AddNonEeaBusinessAssetView
import views.html.asset.{AddAnAssetYesNoView, MaxedOutView}

import scala.concurrent.{ExecutionContext, Future}

class AddAssetsController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  standardActionSets: StandardActionSets,
                                                  repository: PlaybackRepository,
                                                  val appConfig: FrontendAppConfig,
                                                  trustService: TrustService,
                                                  trustsStoreConnector: TrustsStoreConnector,
                                                  @Assets navigator: Navigator,
                                                  addAnotherFormProvider: AddAssetsFormProvider,
                                                  yesNoFormProvider: YesNoFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  addAssetsView: AddNonEeaBusinessAssetView,
                                                  yesNoView: AddAnAssetYesNoView,
                                                  maxedOutView: MaxedOutView,
                                                  errorHandler: ErrorHandler
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val prefix = "addAssets"
  private val addAnotherForm: Form[AddAssets] = addAnotherFormProvider.withPrefix(prefix)
  private val yesNoForm: Form[Boolean] = yesNoFormProvider.withPrefix("addAssetsYesNo")

  private def heading(count: Int)(implicit mp: MessagesProvider): String = {
    count match {
      case c if c > 1 => Messages(s"$prefix.count.heading", c)
      case _ => Messages(s"$prefix.heading")
    }
  }

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      val userAnswers: UserAnswers = request.userAnswers

      for {
        assets <- trustService.getAssets(userAnswers.identifier)
        updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
        _ <- repository.set(updatedAnswers)
      } yield {

        val assetRows = new AddAssetViewHelper(assets).rows

        val maxLimit: Int = userAnswers.isMigratingToTaxable match {
          case true => MAX_ALL_ASSETS
          case _ => MAX_NON_EEA_BUSINESS_ASSETS
        }

        assetRows.count match {
          case 0 =>
            Redirect(controllers.asset.routes.TrustOwnsNonEeaBusinessYesNoController.onPageLoad(NormalMode))
          case c if c >= maxLimit =>
            Ok(maxedOutView(assetRows.inProgress, assetRows.complete, heading(c), maxLimit, prefix))
          case c =>
            Ok(addAssetsView(addAnotherForm, assetRows.inProgress, assetRows.complete, heading(c)))
        }
      }
  }

  def submitOne(): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      yesNoForm.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(BadRequest(yesNoView(formWithErrors)))
        },
        value => {
          for {
            cleanedAnswers <- Future.fromTry(request.userAnswers.cleanup)
            updatedAnswers <- Future.fromTry(cleanedAnswers.set(AddAnAssetYesNoPage, value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddAnAssetYesNoPage, NormalMode, updatedAnswers))
        }
      )
  }

  def submitAnother(): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      trustService.getAssets(request.userAnswers.identifier).flatMap { assets =>
        addAnotherForm.bindFromRequest().fold(
          (formWithErrors: Form[_]) => {

            val assetRows = new AddAssetViewHelper(assets).rows

            Future.successful(BadRequest(addAssetsView(formWithErrors, assetRows.inProgress, assetRows.complete, heading(assetRows.count))))
          },
          {
            value => {
              for {
                cleanedAnswers <- Future.fromTry(request.userAnswers.cleanup)
                updatedAnswers <- Future.fromTry(cleanedAnswers.set(AddAssetsPage, value))
                _ <- repository.set(updatedAnswers)
              } yield {
                Redirect(navigator.nextPage(AddAssetsPage, NormalMode, updatedAnswers))
              }
            }
          }
        )
      } recoverWith {
        case e =>
          logger.error(s"[Session ID: ${utils.Session.id(hc)}][UTR: ${request.userAnswers.identifier}]" +
            s" unable add a new asset due to an error getting assets from trusts ${e.getMessage}")

          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
      }
  }

  def submitComplete(): Action[AnyContent] = standardActionSets.identifiedUserWithData.async {
    implicit request =>

      for {
        _ <- trustsStoreConnector.setTaskComplete(request.userAnswers.identifier)
      } yield {
        Redirect(appConfig.maintainATrustOverview)
      }
  }
}
