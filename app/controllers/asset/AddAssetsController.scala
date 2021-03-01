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

package controllers.asset

import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import forms.{AddAssetsFormProvider, YesNoFormProvider}
import models.AddAssets.NoComplete
import models.Constants._
import models.requests.RegistrationDataRequest
import models.{AddAssets, UserAnswers}
import navigation.Navigator
import pages.asset.{AddAnAssetYesNoPage, AddAssetsPage}
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi, MessagesProvider}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import utils.{AddAssetViewHelper, CheckAnswersFormatters}
import views.html.asset.{AddAnAssetYesNoView, AddAssetsView, MaxedOutView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddAssetsController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     repository: RegistrationsRepository,
                                     navigator: Navigator,
                                     identify: RegistrationIdentifierAction,
                                     getData: DraftIdRetrievalActionProvider,
                                     requireData: RegistrationDataRequiredAction,
                                     addAnotherFormProvider: AddAssetsFormProvider,
                                     yesNoFormProvider: YesNoFormProvider,
                                     val controllerComponents: MessagesControllerComponents,
                                     addAssetsView: AddAssetsView,
                                     yesNoView: AddAnAssetYesNoView,
                                     maxedOutView: MaxedOutView,
                                     checkAnswersFormatters: CheckAnswersFormatters
                                   )(implicit ec: ExecutionContext) extends AddAssetController {

  private def addAnotherForm(isTaxable: Boolean): Form[AddAssets] = addAnotherFormProvider.withPrefix(prefix(isTaxable))
  private val yesNoForm: Form[Boolean] = yesNoFormProvider.withPrefix("addAnAssetYesNo")

  private def actions(draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen getData(draftId) andThen requireData

  private def prefix(isTaxable: Boolean) = "addAssets" + (if (!isTaxable) ".nonTaxable")

  private def heading(count: Int, isTaxable: Boolean)(implicit mp: MessagesProvider): String = {
    val p = prefix(isTaxable)
    count match {
      case c if c > 1 => Messages(s"$p.count.heading", c)
      case _ => Messages(s"$p.heading")
    }
  }

  def onPageLoad(draftId: String): Action[AnyContent] = actions(draftId) {
    implicit request =>

      val userAnswers: UserAnswers = request.userAnswers
      val isTaxable = userAnswers.isTaxable

      val assets = new AddAssetViewHelper(checkAnswersFormatters)(userAnswers, draftId).rows

      val maxLimit: Int = (userAnswers.is5mldEnabled, isTaxable) match {
        case (true, true) => MAX_5MLD_TAXABLE_ASSETS
        case (true, false) => MAX_5MLD_NON_TAXABLE_ASSETS
        case _ => MAX_4MLD_ASSETS
      }

      assets.count match {
        case 0 if isTaxable =>
          Ok(yesNoView(yesNoForm, draftId))
        case 0 =>
          Redirect(routes.TrustOwnsNonEeaBusinessYesNoController.onPageLoad(draftId))
        case c if c >= maxLimit =>
          Ok(maxedOutView(draftId, assets.inProgress, assets.complete, heading(c, isTaxable), maxLimit, prefix(isTaxable)))
        case c =>
          Ok(addAssetsView(addAnotherForm(isTaxable), draftId, assets.inProgress, assets.complete, heading(c, isTaxable)))
      }
  }

  def submitOne(draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      yesNoForm.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(BadRequest(yesNoView(formWithErrors, draftId)))
        },
        value => {
          for {
            answersWithAssetTypeIfNonTaxable <- Future.fromTry(setAssetTypeIfNonTaxable(request.userAnswers, 0))
            updatedAnswers <- Future.fromTry(answersWithAssetTypeIfNonTaxable.set(AddAnAssetYesNoPage, value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddAnAssetYesNoPage, draftId)(updatedAnswers))
        }
      )
  }

  def submitAnother(draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      val userAnswers = request.userAnswers
      val isTaxable = userAnswers.isTaxable

      val assets = new AddAssetViewHelper(checkAnswersFormatters)(userAnswers, draftId).rows

      addAnotherForm(isTaxable).bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(
            BadRequest(addAssetsView(formWithErrors, draftId, assets.inProgress, assets.complete, heading(assets.count, isTaxable)))
          )
        },
        value => {
          for {
            answersWithAssetTypeIfNonTaxable <- Future.fromTry(setAssetTypeIfNonTaxable(userAnswers, assets.count, value))
            updatedAnswers <- Future.fromTry(answersWithAssetTypeIfNonTaxable.set(AddAssetsPage, value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddAssetsPage, draftId)(updatedAnswers))
        }
      )
  }

  def submitComplete(draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAssetsPage, NoComplete))
        _              <- repository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(AddAssetsPage, draftId)(updatedAnswers))
  }
}
