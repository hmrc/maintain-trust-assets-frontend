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

package controllers.asset.business

import config.annotations.Business
import controllers.actions.{AuthenticatedIdentifierAction, DraftIdDataRetrievalAction, RegistrationDataRequiredAction, RequiredAnswerActionProvider, StandardActionSets}
import controllers.filters.IndexActionFilterProvider
import forms.NameFormProvider
import navigation.Navigator
import pages.asset.business.BusinessNamePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.business.BusinessNameView

import javax.inject.Inject
import models.Mode
import models.requests.DataRequest
import sections.Assets

import scala.concurrent.{ExecutionContext, Future}

class BusinessNameController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        standardActionSets: StandardActionSets,
                                        repository: PlaybackRepository,
                                        identify: AuthenticatedIdentifierAction,
                                        getData: DraftIdDataRetrievalAction,
//                                        requiredAnswer: RequiredAnswerActionProvider,
                                        requireData: RegistrationDataRequiredAction,
                                        validateIndex: IndexActionFilterProvider,
                                        @Business navigator: Navigator,
                                        formProvider: NameFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: BusinessNameView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[String] = formProvider.withConfig(105, "business.name")

  private def actions(index: Int, draftId: String): ActionBuilder[DataRequest, AnyContent] =
    identify andThen getData(draftId) andThen
      requireData andThen
      validateIndex(index, Assets)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) { implicit request =>
    val preparedForm = request.userAnswers.get(BusinessNamePage(index)) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, draftId, index))

  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[_]) => Future.successful(BadRequest(view(formWithErrors, draftId, index))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessNamePage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(BusinessNamePage(index), draftId)(updatedAnswers))
      )
  }
}
