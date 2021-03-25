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

import controllers.actions.StandardActionSets
import forms.{AddAssetsFormProvider, YesNoFormProvider}
import models.AddAssets.NoComplete
import models.Constants._
import models.{AddAssets, NormalMode, UserAnswers}
import navigation.Navigator
import pages.asset.{AddAnAssetYesNoPage, AddAssetsPage}
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi, MessagesProvider}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import utils.AddAssetViewHelper
import views.html.asset.{AddAnAssetYesNoView, AddAssetsView, MaxedOutView}
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class AddAssetsController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     standardActionSets: StandardActionSets,
                                     repository: PlaybackRepository,
                                     navigator: Navigator,
                                     addAnotherFormProvider: AddAssetsFormProvider,
                                     yesNoFormProvider: YesNoFormProvider,
                                     val controllerComponents: MessagesControllerComponents,
                                     addAssetsView: AddAssetsView,
                                     yesNoView: AddAnAssetYesNoView,
                                     maxedOutView: MaxedOutView
                                   )(implicit ec: ExecutionContext) extends AddAssetController {

  private def addAnotherForm(isTaxable: Boolean): Form[AddAssets] = addAnotherFormProvider.withPrefix(determinePrefix(isTaxable))
  private val yesNoForm: Form[Boolean] = yesNoFormProvider.withPrefix("addAnAssetYesNo")

  private def determinePrefix(isTaxable: Boolean) = "addAssets" + (if (!isTaxable) ".nonTaxable" else "")

  private def heading(count: Int, prefix: String)(implicit mp: MessagesProvider): String = {
    count match {
      case c if c > 1 => Messages(s"$prefix.count.heading", c)
      case _ => Messages(s"$prefix.heading")
    }
  }

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>

      val userAnswers: UserAnswers = request.userAnswers
      val isTaxable = userAnswers.isTaxable

      val assets = new AddAssetViewHelper(userAnswers).rows

      val maxLimit: Int = (userAnswers.is5mldEnabled, isTaxable) match {
        case (true, true) => MAX_5MLD_TAXABLE_ASSETS
        case (true, false) => MAX_5MLD_NON_TAXABLE_ASSETS
        case _ => MAX_4MLD_ASSETS
      }

      val prefix = determinePrefix(isTaxable)

      assets.count match {
        case 0 if isTaxable =>
          Ok(yesNoView(yesNoForm))
        case 0 =>
          Redirect(routes.TrustOwnsNonEeaBusinessYesNoController.onPageLoad(NormalMode))
        case c if c >= maxLimit =>
          Ok(maxedOutView(assets.inProgress, assets.complete, heading(c, prefix), maxLimit, prefix))
        case c =>
          Ok(addAssetsView(addAnotherForm(isTaxable), assets.inProgress, assets.complete, heading(c, prefix), prefix))
      }
  }

  def submitOne(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      yesNoForm.bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(BadRequest(yesNoView(formWithErrors)))
        },
        value => {
          for {
            answersWithAssetTypeIfNonTaxable <- Future.fromTry(setAssetTypeIfNonTaxable(request.userAnswers, 0))
            updatedAnswers <- Future.fromTry(answersWithAssetTypeIfNonTaxable.set(AddAnAssetYesNoPage, value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddAnAssetYesNoPage, NormalMode, updatedAnswers))
        }
      )
  }

  def submitAnother(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      val userAnswers = request.userAnswers
      val isTaxable = userAnswers.isTaxable

      val assets = new AddAssetViewHelper(userAnswers).rows

      val prefix = determinePrefix(isTaxable)

      addAnotherForm(isTaxable).bindFromRequest().fold(
        (formWithErrors: Form[_]) => {
          Future.successful(
            BadRequest(addAssetsView(formWithErrors, assets.inProgress, assets.complete, heading(assets.count, prefix), prefix))
          )
        },
        value => {
          for {
            answersWithAssetTypeIfNonTaxable <- Future.fromTry(setAssetTypeIfNonTaxable(userAnswers, assets.count, value))
            updatedAnswers <- Future.fromTry(answersWithAssetTypeIfNonTaxable.set(AddAssetsPage, value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddAssetsPage, NormalMode, updatedAnswers))
        }
      )
  }

  def submitComplete(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAssetsPage, NoComplete))
        _              <- repository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(AddAssetsPage, NormalMode, updatedAnswers))
  }
}
