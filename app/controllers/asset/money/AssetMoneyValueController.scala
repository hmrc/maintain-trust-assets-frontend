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

package controllers.asset.money

import config.annotations.Money
import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import forms.ValueFormProvider
import models.Status.Completed
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.AssetStatus
import pages.asset.money.AssetMoneyValuePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.asset.money.AssetMoneyValueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AssetMoneyValueController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           repository: RegistrationsRepository,
                                           @Money navigator: Navigator,
                                           identify: RegistrationIdentifierAction,
                                           getData: DraftIdRetrievalActionProvider,
                                           requireData: RegistrationDataRequiredAction,
                                           formProvider: ValueFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: AssetMoneyValueView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] = identify andThen getData(draftId) andThen requireData

  private val form: Form[Long] = formProvider.withConfig(prefix = "money.value")

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AssetMoneyValuePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, draftId, index))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId, index))),

        value => {

          val answers = request.userAnswers.set(AssetMoneyValuePage(index), value)
            .flatMap(_.set(AssetStatus(index), Completed))

          for {
                updatedAnswers <- Future.fromTry(answers)
                _              <- repository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AssetMoneyValuePage(index), draftId)(updatedAnswers))
          }
      )
  }
}
