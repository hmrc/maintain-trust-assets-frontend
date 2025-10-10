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

package controllers.asset.business

import config.annotations.Business
import controllers.actions._
import controllers.actions.business.NameRequiredAction
import forms.ValueFormProvider
import models.Mode
import navigation.Navigator
import pages.asset.business.BusinessValuePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.business.BusinessValueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessValueController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         standardActionSets: StandardActionSets,
                                         nameAction: NameRequiredAction,
                                         repository: PlaybackRepository,
                                         @Business navigator: Navigator,
                                         formProvider: ValueFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: BusinessValueView
                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Long] = formProvider.withConfig(prefix = "business.currentValue")

  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] =
    (standardActionSets.verifiedForIdentifier andThen nameAction) { implicit request =>
      val preparedForm = request.userAnswers.get(BusinessValuePage(index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, index, mode, request.name))
    }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] =
    (standardActionSets.verifiedForIdentifier andThen nameAction).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, index, mode, request.name))),
        value => {
          val answers = request.userAnswers.set(BusinessValuePage(index), value)
          for {
            updatedAnswers <- Future.fromTry(answers)
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(BusinessValuePage(index), mode, updatedAnswers))
        }
      )
    }

}
