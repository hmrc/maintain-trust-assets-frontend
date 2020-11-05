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

import controllers.actions._
import controllers.filters.IndexActionFilterProvider
import forms.ValueFormProvider
import javax.inject.Inject
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.asset.shares.{ShareCompanyNamePage, ShareValueInTrustPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.Shares
import views.html.asset.shares.ShareValueInTrustView

import scala.concurrent.{ExecutionContext, Future}

class ShareValueInTrustController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             repository: RegistrationsRepository,
                                             @Shares navigator: Navigator,
                                             identify: RegistrationIdentifierAction,
                                             getData: DraftIdRetrievalActionProvider,
                                             requireData: RegistrationDataRequiredAction,
                                             formProvider: ValueFormProvider,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: ShareValueInTrustView,
                                             requiredAnswer: RequiredAnswerActionProvider,
                                             validateIndex: IndexActionFilterProvider
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider.withConfig("shares.valueInTrust")

  private def actions(index : Int, draftId: String) =
    identify andThen getData(draftId) andThen
      requireData andThen
      validateIndex(index, sections.Assets) andThen
      requiredAnswer(RequiredAnswer(
        ShareCompanyNamePage(index),
        routes.ShareCompanyNameController.onPageLoad(NormalMode, index, draftId)
      ))

  def onPageLoad(mode: Mode, index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val companyName = request.userAnswers.get(ShareCompanyNamePage(index)).get

      val preparedForm = request.userAnswers.get(ShareValueInTrustPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, draftId, index, companyName))
  }

  def onSubmit(mode: Mode, index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      val companyName = request.userAnswers.get(ShareCompanyNamePage(index)).get

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, draftId, index, companyName))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ShareValueInTrustPage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ShareValueInTrustPage(index), mode, draftId)(updatedAnswers))
        }
      )
  }
}
