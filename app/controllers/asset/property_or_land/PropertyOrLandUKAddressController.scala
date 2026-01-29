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

package controllers.asset.property_or_land

import config.annotations.PropertyOrLand
import controllers.actions.property_or_land.NameRequiredAction
import controllers.actions.StandardActionSets
import forms.UKAddressFormProvider
import navigation.Navigator
import pages.asset.property_or_land.PropertyOrLandUKAddressPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.property_or_land.PropertyOrLandUKAddressView
import javax.inject.Inject
import models.Mode

import scala.concurrent.{ExecutionContext, Future}

class PropertyOrLandUKAddressController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  nameAction: NameRequiredAction,
  repository: PlaybackRepository,
  @PropertyOrLand navigator: Navigator,
  formProvider: UKAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PropertyOrLandUKAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] =
    (standardActionSets.verifiedForIdentifier andThen nameAction) { implicit request =>
      val preparedForm = request.userAnswers.get(PropertyOrLandUKAddressPage(index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, index, mode))
    }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] =
    (standardActionSets.verifiedForIdentifier andThen nameAction).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[_]) => Future.successful(BadRequest(view(formWithErrors, index, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(PropertyOrLandUKAddressPage(index), value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(PropertyOrLandUKAddressPage(index), mode, updatedAnswers))
        )
    }

}
