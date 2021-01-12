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

package controllers.asset.other

import config.annotations.Other
import controllers.actions._
import forms.ValueFormProvider
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.asset.other.{OtherAssetDescriptionPage, OtherAssetValuePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.asset.other.OtherAssetValueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherAssetValueController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           repository: RegistrationsRepository,
                                           @Other navigator: Navigator,
                                           identify: RegistrationIdentifierAction,
                                           getData: DraftIdRetrievalActionProvider,
                                           requireData: RegistrationDataRequiredAction,
                                           requiredAnswer: RequiredAnswerActionProvider,
                                           formProvider: ValueFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: OtherAssetValueView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(index: Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] = {
    identify andThen getData(draftId) andThen requireData andThen
      requiredAnswer(RequiredAnswer(OtherAssetDescriptionPage(index), routes.OtherAssetDescriptionController.onPageLoad(index, draftId)))
  }

  private val form: Form[Long] = formProvider.withConfig(prefix = "other.value")

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(OtherAssetValuePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, draftId, index, description(index)))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId, index, description(index)))),

        value => {

          val answers = request.userAnswers.set(OtherAssetValuePage(index), value)

          for {
            updatedAnswers <- Future.fromTry(answers)
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(OtherAssetValuePage(index), draftId)(updatedAnswers))
        }
      )
  }

  private def description(index: Int)(implicit request: RegistrationDataRequest[AnyContent]) = {
    request.userAnswers.get(OtherAssetDescriptionPage(index)).get
  }
}
