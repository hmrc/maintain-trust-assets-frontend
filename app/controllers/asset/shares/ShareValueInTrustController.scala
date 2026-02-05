/*
 * Copyright 2025 HM Revenue & Customs
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
import forms.ValueFormProvider
import navigation.Navigator
import pages.asset.shares.ShareValueInTrustPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.shares.ShareValueInTrustView
import javax.inject.Inject
import models.Mode

import scala.concurrent.{ExecutionContext, Future}

class ShareValueInTrustController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  nameAction: CompanyNameRequiredAction,
  repository: PlaybackRepository,
  @Shares navigator: Navigator,
  formProvider: ValueFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ShareValueInTrustView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private val form = formProvider.withConfig(prefix = "shares.valueInTrust")

  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] =
    (standardActionSets.verifiedForIdentifier andThen nameAction) { implicit request =>
      val preparedForm = request.userAnswers.get(ShareValueInTrustPage(index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, index, mode, request.name))
    }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] =
    (standardActionSets.verifiedForIdentifier andThen nameAction).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[_]) => Future.successful(BadRequest(view(formWithErrors, index, mode, request.name))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ShareValueInTrustPage(index), value))
              _              <- repository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ShareValueInTrustPage(index), mode, updatedAnswers))
        )
    }

}
