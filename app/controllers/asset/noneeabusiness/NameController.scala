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

package controllers.asset.noneeabusiness

import config.annotations.NonEeaBusiness
import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import controllers.filters.IndexActionFilterProvider
import forms.NameFormProvider
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.asset.noneeabusiness.NamePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import sections.Assets
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.noneeabusiness.NameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NameController @Inject()(
                                override val messagesApi: MessagesApi,
                                registrationsRepository: RegistrationsRepository,
                                @NonEeaBusiness navigator: Navigator,
                                identify: RegistrationIdentifierAction,
                                getData: DraftIdRetrievalActionProvider,
                                requireData: RegistrationDataRequiredAction,
                                validateIndex: IndexActionFilterProvider,
                                formProvider: NameFormProvider,
                                val controllerComponents: MessagesControllerComponents,
                                view: NameView
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[String] = formProvider.withConfig(105, "nonEeaBusiness.name")

  private def actions(index: Int): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen getData() andThen
      requireData andThen
      validateIndex(index, Assets)

  def onPageLoad(index: Int): Action[AnyContent] = actions(index) {
    implicit request =>

      val preparedForm = request.userAnswers.get(NamePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, index))

  }

  def onSubmit(index: Int): Action[AnyContent] = actions(index).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, index))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NamePage(index), value))
            _ <- registrationsRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(NamePage(index))(updatedAnswers))
        }
      )
  }
}
