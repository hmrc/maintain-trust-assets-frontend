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

package controllers.asset.partnership

import config.annotations.Partnership
import controllers.actions.StandardActionSets
import forms.DescriptionFormProvider
import navigation.Navigator
import pages.asset.partnership.PartnershipDescriptionPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.partnership.PartnershipDescriptionView
import javax.inject.Inject
import models.Mode

import scala.concurrent.{ExecutionContext, Future}

class PartnershipDescriptionController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  standardActionSets: StandardActionSets,
                                                  repository: PlaybackRepository,
                                                  @Partnership navigator: Navigator,
                                                  formProvider: DescriptionFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: PartnershipDescriptionView
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider.withConfig(56, "partnership.description")

  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>
      val preparedForm = request.userAnswers.get(PartnershipDescriptionPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, index, mode))
  }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, index, mode))),
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnershipDescriptionPage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PartnershipDescriptionPage(index), mode, updatedAnswers))
        }
      )
  }
}
