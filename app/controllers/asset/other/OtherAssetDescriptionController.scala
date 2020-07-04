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

package controllers.asset.other

import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import forms.DescriptionFormProvider
import javax.inject.Inject
import models.Mode
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.asset.other.OtherAssetDescriptionPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.Other
import views.html.asset.other.OtherAssetDescriptionView

import scala.concurrent.{ExecutionContext, Future}

class OtherAssetDescriptionController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 repository: RegistrationsRepository,
                                                 @Other navigator: Navigator,
                                                 identify: RegistrationIdentifierAction,
                                                 getData: DraftIdRetrievalActionProvider,
                                                 requireData: RegistrationDataRequiredAction,
                                                 formProvider: DescriptionFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: OtherAssetDescriptionView
                                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] = identify andThen getData(draftId) andThen requireData

  val form: Form[String] = formProvider.withConfig(length = 56, prefix = "assets.other.description")

  def onPageLoad(mode: Mode, index: Int, draftId: String): Action[AnyContent] = actions(draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(OtherAssetDescriptionPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, draftId, index))
  }

  def onSubmit(mode: Mode, index: Int, draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, draftId, index))),

        value => {

          val answers = request.userAnswers.set(OtherAssetDescriptionPage(index), value)

          for {
            updatedAnswers <- Future.fromTry(answers)
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(OtherAssetDescriptionPage(index), mode, draftId)(updatedAnswers))
        }
      )
  }
}
