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

package controllers.asset.noneeabusiness.add

import config.annotations.NonEeaBusiness
import controllers.actions._
import controllers.actions.noneeabusiness.NameRequiredAction
import forms.StartDateFormProvider
import models.NormalMode
import navigation.Navigator
import pages.asset.noneeabusiness.add.StartDatePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.noneeabusiness.add.StartDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartDateController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     standardActionSets: StandardActionSets,
                                     nameAction: NameRequiredAction,
                                     repository: PlaybackRepository,
                                     @NonEeaBusiness navigator: Navigator,
                                     formProvider: StartDateFormProvider,
                                     val controllerComponents: MessagesControllerComponents,
                                     view: StartDateView
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val messagePrefix: String = "nonEeaBusiness.startDate"

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>

      val form = formProvider.withConfig(messagePrefix, request.userAnswers.whenTrustSetup)
      val preparedForm = request.userAnswers.get(StartDatePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, request.name))
  }

  def onSubmit(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction).async {
    implicit request =>

      val form = formProvider.withConfig(messagePrefix, request.userAnswers.whenTrustSetup)
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, request.name))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(StartDatePage, value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(StartDatePage, NormalMode, updatedAnswers))
        }
      )
  }
}
