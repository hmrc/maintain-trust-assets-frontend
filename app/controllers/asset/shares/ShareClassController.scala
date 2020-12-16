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

package controllers.asset.shares

import config.annotations.Shares
import controllers.actions._
import controllers.filters.IndexActionFilterProvider
import forms.shares.ShareClassFormProvider
import models.Enumerable
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.asset.shares.{ShareClassPage, ShareCompanyNamePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.asset.shares.ShareClassView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ShareClassController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      repository: RegistrationsRepository,
                                      @Shares navigator: Navigator,
                                      identify: RegistrationIdentifierAction,
                                      getData: DraftIdRetrievalActionProvider,
                                      requireData: RegistrationDataRequiredAction,
                                      formProvider: ShareClassFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: ShareClassView,
                                      validateIndex: IndexActionFilterProvider,
                                      requiredAnswer: RequiredAnswerActionProvider
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  private def actions(index : Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen getData(draftId) andThen
      requireData andThen
      validateIndex(index, sections.Assets) andThen
      requiredAnswer(RequiredAnswer(
        ShareCompanyNamePage(index),
        routes.ShareCompanyNameController.onPageLoad(index, draftId)
      ))

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val companyName = request.userAnswers.get(ShareCompanyNamePage(index)).get

      val preparedForm = request.userAnswers.get(ShareClassPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, draftId, index, companyName))
  }

  def onSubmit(index: Int,  draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      val companyName = request.userAnswers.get(ShareCompanyNamePage(index)).get

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId, index, companyName))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ShareClassPage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ShareClassPage(index), draftId)(updatedAnswers))
        }
      )
  }
}
