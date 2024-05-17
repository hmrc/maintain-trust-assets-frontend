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

package controllers.asset.shares

import config.annotations.Shares
import controllers.actions._
import controllers.actions.shares.CompanyNameRequiredAction
import forms.shares.ShareClassFormProvider
import models.{Enumerable, Mode}
import navigation.Navigator
import pages.asset.shares.ShareClassPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.shares.ShareClassView
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class ShareClassController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      standardActionSets: StandardActionSets,
                                      nameAction: CompanyNameRequiredAction,
                                      repository: PlaybackRepository,
                                      @Shares navigator: Navigator,
                                      formProvider: ShareClassFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: ShareClassView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ShareClassPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, request.name))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, request.name))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ShareClassPage, value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ShareClassPage, mode, updatedAnswers))
        }
      )
  }
}
