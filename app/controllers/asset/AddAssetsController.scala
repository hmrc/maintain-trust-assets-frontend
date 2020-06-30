/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.asset

import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import forms.{AddAssetsFormProvider, YesNoFormProvider}
import javax.inject.Inject
import models.{AddAssets, Enumerable, Mode}
import navigation.Navigator
import pages.asset.{AddAnAssetYesNoPage, AddAssetsPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi, MessagesProvider}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.AssetsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.AddAssetViewHelper
import views.html.asset.{AddAnAssetYesNoView, AddAssetsView}

import scala.concurrent.{ExecutionContext, Future}

class AddAssetsController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     repository: AssetsRepository,
                                     navigator: Navigator,
                                     identify: RegistrationIdentifierAction,
                                     getData: DraftIdRetrievalActionProvider,
                                     requireData: RegistrationDataRequiredAction,
                                     addAnotherFormProvider: AddAssetsFormProvider,
                                     yesNoFormProvider: YesNoFormProvider,
                                     val controllerComponents: MessagesControllerComponents,
                                     addAssetsView: AddAssetsView,
                                     yesNoView: AddAnAssetYesNoView
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  private val addAnotherForm: Form[AddAssets] = addAnotherFormProvider()
  private val yesNoForm: Form[Boolean] = yesNoFormProvider.withPrefix("addAnAssetYesNo")

  private def actions(draftId: String) =
    identify andThen getData(draftId) andThen requireData

  private def heading(count: Int)(implicit mp : MessagesProvider) = {
    count match {
      case 0 => Messages("addAssets.heading")
      case 1 => Messages("addAssets.singular.heading")
      case size => Messages("addAssets.count.heading", size)
    }
  }

  def onPageLoad(mode: Mode, draftId: String): Action[AnyContent] = actions(draftId) {
    implicit request =>

      val assets = new AddAssetViewHelper(request.userAnswers, mode, draftId).rows

      val count = assets.count

      count match {
        case 0 => Ok(yesNoView(addAnotherForm, mode, draftId))
        case _ => Ok(addAssetsView(addAnotherForm, mode, draftId, assets.inProgress, assets.complete, heading(count)))
      }
  }

  def submitOne(mode: Mode, draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      yesNoForm.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(BadRequest(yesNoView(formWithErrors, mode, draftId)))
        },
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAnAssetYesNoPage, value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddAnAssetYesNoPage, mode, draftId)(updatedAnswers))
        }
      )
  }

  def submitAnother(mode: Mode, draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      addAnotherForm.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          val assets = new AddAssetViewHelper(request.userAnswers, mode, draftId).rows

          val count = assets.count

          Future.successful(BadRequest(addAssetsView(formWithErrors, mode, draftId, assets.inProgress, assets.complete, heading(count))))
        },
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAssetsPage, value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddAssetsPage, mode, draftId)(updatedAnswers))
        }
      )
  }
}
