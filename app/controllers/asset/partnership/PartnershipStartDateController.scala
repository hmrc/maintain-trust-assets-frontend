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

package controllers.asset.partnership

import config.annotations.Partnership
import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import controllers.filters.IndexActionFilterProvider
import forms.partnership.PartnershipStartDateFormProvider
import navigation.Navigator
import pages.asset.partnership.PartnershipStartDatePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.partnership.PartnershipStartDateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipStartDateController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                repository: RegistrationsRepository,
                                                @Partnership navigator: Navigator,
                                                identify: RegistrationIdentifierAction,
                                                getData: DraftIdRetrievalActionProvider,
                                                validateIndex: IndexActionFilterProvider,
                                                requireData: RegistrationDataRequiredAction,
                                                formProvider: PartnershipStartDateFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: PartnershipStartDateView
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  private def actions(index: Int, draftId: String) =
    identify andThen
      getData(draftId) andThen
      requireData andThen
      validateIndex(index, sections.Assets)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PartnershipStartDatePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, index, draftId))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, index, draftId))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnershipStartDatePage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PartnershipStartDatePage(index), draftId)(updatedAnswers))
        }
      )
  }
}
