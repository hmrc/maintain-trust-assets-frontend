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

package controllers

import forms.RemoveForm
import models.requests.RegistrationDataRequest
import pages.QuestionPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, Call}
import play.twirl.api.HtmlFormat
import queries.Settable
import repositories.AssetsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.RemoveIndexView

import scala.concurrent.Future

trait RemoveIndexController extends FrontendBaseController with I18nSupport {

  val messagesPrefix : String

  val formProvider : RemoveForm

  val removeView: RemoveIndexView

  lazy val form: Form[Boolean] = formProvider.apply(messagesPrefix)

  def page(index: Int) : QuestionPage[_]

  def repository : AssetsRepository

  def actions(draftId: String, index: Int) : ActionBuilder[RegistrationDataRequest, AnyContent]

  def redirect(draftId : String) : Call

  def formRoute(draftId : String, index: Int) : Call

  def removeQuery(index : Int) : Settable[_]

  def content(index: Int)(implicit request: RegistrationDataRequest[AnyContent]) : String

  def view(form: Form[_], index: Int, draftId: String)
                   (implicit request: RegistrationDataRequest[AnyContent], messagesApi: MessagesApi): HtmlFormat.Appendable = {
    removeView(messagesPrefix, form, index, draftId, content(index), formRoute(draftId, index))
  }

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(draftId, index) {
    implicit request =>
      Ok(view(form, index, draftId))
  }

  def onSubmit(index: Int, draftId : String): Action[AnyContent] = actions(draftId, index).async {
    implicit request =>

      import scala.concurrent.ExecutionContext.Implicits._

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, index, draftId))),
        value => {
          if (value) {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.remove(removeQuery(index)))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(redirect(draftId).url)
          } else {
            Future.successful(Redirect(redirect(draftId).url))
          }
        }
      )
  }

}
