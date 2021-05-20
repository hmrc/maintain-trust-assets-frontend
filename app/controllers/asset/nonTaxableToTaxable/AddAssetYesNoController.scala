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

import config.annotations.Assets
import connectors.TrustsStoreConnector
import controllers.actions.StandardActionSets
import forms.YesNoFormProvider

import javax.inject.Inject
import navigation.Navigator
import pages.asset.nontaxabletotaxable.AddAssetsYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.nonTaxableToTaxable.AddAssetYesNoView

import scala.concurrent.{ExecutionContext, Future}

class AddAssetYesNoController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          standardActionSets: StandardActionSets,
                                          repository: PlaybackRepository,
                                          @Assets navigator: Navigator,
                                          yesNoFormProvider: YesNoFormProvider,
                                          trustService: TrustService,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: AddAssetYesNoView,
                                          trustStoreConnector: TrustsStoreConnector
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = yesNoFormProvider.withPrefix("nonTaxableToTaxable.addAssetYesNo")

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AddAssetsYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors))),

        value => {

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAssetsYesNoPage, value))
            _              <- repository.set(updatedAnswers)
            assets <- trustService.getAssets(request.userAnswers.identifier)
            _ <- if (!value) { // If answered no don't want to add
              assets match {
                case x if x.isEmpty =>
                  // If no assets, set task list to in progress
                  trustStoreConnector.setTaskInProgress(request.userAnswers.identifier).map(_ => ())
                case _ =>
                  // Has a taxable asset or Non-EEA company
                  trustStoreConnector.setTaskComplete(request.userAnswers.identifier).map(_ => ())
              }
            } else {
              // Do nothing and continue in the journey if adding an asset, status is decided later
              Future.successful(())
            }
          } yield Redirect(navigator.nextPage(AddAssetsYesNoPage, updatedAnswers, assets))
        }
      )
  }
}
