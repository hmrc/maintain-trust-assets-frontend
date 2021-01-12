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

package controllers.asset.shares

import config.annotations.Shares
import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import controllers.filters.IndexActionFilterProvider
import forms.NameFormProvider
import navigation.Navigator
import pages.asset.shares.ShareCompanyNamePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.asset.shares.ShareCompanyNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ShareCompanyNameController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            repository: RegistrationsRepository,
                                            @Shares navigator: Navigator,
                                            identify: RegistrationIdentifierAction,
                                            getData: DraftIdRetrievalActionProvider,
                                            requireData: RegistrationDataRequiredAction,
                                            formProvider: NameFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: ShareCompanyNameView,
                                            validateIndex: IndexActionFilterProvider
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(index : Int, draftId: String) =
    identify andThen getData(draftId) andThen
      requireData andThen
      validateIndex(index, sections.Assets)

  private val form = formProvider.withConfig(53, "shares.companyName")

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ShareCompanyNamePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, draftId, index))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId, index))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ShareCompanyNamePage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ShareCompanyNamePage(index), draftId)(updatedAnswers))
        }
      )
  }
}
