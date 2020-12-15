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

package controllers.asset.business

import config.annotations.Business
import controllers.actions._
import controllers.filters.IndexActionFilterProvider
import forms.DescriptionFormProvider
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.asset.business.{BusinessDescriptionPage, BusinessNamePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import sections.Assets
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.asset.buisness.BusinessDescriptionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessDescriptionController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               registrationsRepository: RegistrationsRepository,
                                               @Business navigator: Navigator,
                                               identify: RegistrationIdentifierAction,
                                               getData: DraftIdRetrievalActionProvider,
                                               requireData: RegistrationDataRequiredAction,
                                               validateIndex: IndexActionFilterProvider,
                                               formProvider: DescriptionFormProvider,
                                               requiredAnswer: RequiredAnswerActionProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: BusinessDescriptionView
                                             )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[String] = formProvider.withConfig(length = 56, prefix = "business.description")

  private def actions(index: Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen getData(draftId) andThen
      requireData andThen
      validateIndex(index, Assets) andThen
      requiredAnswer(RequiredAnswer(BusinessNamePage(index), routes.BusinessNameController.onPageLoad(index, draftId)))

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val assetDescription = request.userAnswers.get(BusinessNamePage(index)).get

      val preparedForm = request.userAnswers.get(BusinessDescriptionPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, draftId, index, assetDescription))

  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      val assetDescription = request.userAnswers.get(BusinessNamePage(index)).get

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId, index, assetDescription))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessDescriptionPage(index), value))
            _ <- registrationsRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(BusinessDescriptionPage(index), draftId)(updatedAnswers))
        }
      )
  }
}
