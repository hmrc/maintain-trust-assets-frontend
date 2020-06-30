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

package controllers.asset.partnership

import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import controllers.filters.IndexActionFilterProvider
import forms.partnership.PartnershipDescriptionFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.asset.partnership.PartnershipDescriptionPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.AssetsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.Partnership
import views.html.asset.partnership.PartnershipDescriptionView

import scala.concurrent.{ExecutionContext, Future}

class PartnershipDescriptionController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  repository: AssetsRepository,
                                                  @Partnership navigator: Navigator,
                                                  identify: RegistrationIdentifierAction,
                                                  getData: DraftIdRetrievalActionProvider,
                                                  requireData: RegistrationDataRequiredAction,
                                                  validateIndex: IndexActionFilterProvider,
                                                  formProvider: PartnershipDescriptionFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: PartnershipDescriptionView
                                    )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  private def actions(index: Int, draftId: String) =
    identify andThen
      getData(draftId) andThen
      requireData andThen
      validateIndex(index, sections.Assets)

  def onPageLoad(mode: Mode, index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PartnershipDescriptionPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, index, draftId))
  }

  def onSubmit(mode: Mode, index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, index, draftId))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnershipDescriptionPage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PartnershipDescriptionPage(index), mode, draftId)(updatedAnswers))
        }
      )
  }
}
