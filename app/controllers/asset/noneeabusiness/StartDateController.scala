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
import controllers.actions._
import controllers.filters.IndexActionFilterProvider
import forms.StartDateFormProvider
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.asset.noneeabusiness.{NamePage, StartDatePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.noneeabusiness.StartDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartDateController @Inject()(
                                     override val messagesApi: MessagesApi,
                                     repository: RegistrationsRepository,
                                     @NonEeaBusiness navigator: Navigator,
                                     identify: RegistrationIdentifierAction,
                                     getData: DraftIdRetrievalActionProvider,
                                     validateIndex: IndexActionFilterProvider,
                                     requireData: RegistrationDataRequiredAction,
                                     requiredAnswer: RequiredAnswerActionProvider,
                                     formProvider: StartDateFormProvider,
                                     val controllerComponents: MessagesControllerComponents,
                                     view: StartDateView
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider.withPrefix("nonEeaBusiness.startDate")

  private def actions(index: Int): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen
      getData() andThen
      requireData andThen
      validateIndex(index, sections.Assets) andThen
      requiredAnswer(RequiredAnswer(NamePage(index), routes.NameController.onPageLoad(index)))

  def onPageLoad(index: Int): Action[AnyContent] = actions(index) {
    implicit request =>

      val name = request.userAnswers.get(NamePage(index)).get

      val preparedForm = request.userAnswers.get(StartDatePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, index, name))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions(index).async {
    implicit request =>

      val name = request.userAnswers.get(NamePage(index)).get

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, index, name))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(StartDatePage(index), value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(StartDatePage(index))(updatedAnswers))
        }
      )
  }
}
