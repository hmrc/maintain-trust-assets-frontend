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

package controllers.asset.taxable

import config.annotations.Taxable
import controllers.actions.StandardActionSets
import controllers.actions.property_or_land.NameRequiredAction
import forms.YesNoFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.asset.taxable.AddAssetsYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.taxable.AddAssetsYesNoView

import scala.concurrent.{ExecutionContext, Future}

class AddAssetsYesNoController @Inject()(
                                                      override val messagesApi: MessagesApi,
                                                      standardActionSets: StandardActionSets,
                                                      nameAction: NameRequiredAction,
                                                      repository: PlaybackRepository,
                                                      @Taxable navigator: Navigator,
                                                      yesNoFormProvider: YesNoFormProvider,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      view: AddAssetsYesNoView
                                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = yesNoFormProvider.withPrefix("taxable.addAssetsYesNo")

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AddAssetsYesNoPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAssetsYesNoPage, value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddAssetsYesNoPage, mode, updatedAnswers))
        }
      )
  }
}
