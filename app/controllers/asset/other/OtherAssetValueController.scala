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

package controllers.asset.other

import config.annotations.Other
import controllers.actions._
import controllers.actions.other.NameRequiredAction
import forms.ValueFormProvider
import models.Mode
import navigation.Navigator
import pages.asset.other.{OtherAssetDescriptionPage, OtherAssetValuePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.other.OtherAssetValueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherAssetValueController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           standardActionSets: StandardActionSets,
                                           nameAction: NameRequiredAction,
                                           repository: PlaybackRepository,
                                           @Other navigator: Navigator,
                                           formProvider: ValueFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: OtherAssetValueView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Long] = formProvider.withConfig(prefix = "other.value")

  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      val description = request.userAnswers.get(OtherAssetDescriptionPage(index)).getOrElse("")
      val preparedForm = request.userAnswers.get(OtherAssetValuePage(index)) match {
        case Some(value) => form.fill(value)
        case None        => form
      }
      Ok(view(preparedForm, index, mode, description))
    }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction).async {
    implicit request =>
      val description = request.userAnswers.get(OtherAssetDescriptionPage(index)).getOrElse("")
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, index, mode, description))),
        value => {
          val answers = request.userAnswers.set(OtherAssetValuePage(index), value)
          for {
            updatedAnswers <- Future.fromTry(answers)
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(OtherAssetValuePage(index), mode, updatedAnswers))
        }
      )
  }
}
