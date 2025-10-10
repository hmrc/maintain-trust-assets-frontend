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

package controllers.asset.other

import config.annotations.Other
import controllers.actions._
import forms.DescriptionFormProvider
import navigation.Navigator
import pages.asset.other.OtherAssetDescriptionPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.other.OtherAssetDescriptionView
import javax.inject.Inject
import models.Mode

import scala.concurrent.{ExecutionContext, Future}

class OtherAssetDescriptionController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 standardActionSets: StandardActionSets,
                                                 repository: PlaybackRepository,
                                                 @Other navigator: Navigator,
                                                 formProvider: DescriptionFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: OtherAssetDescriptionView
                                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[String] = formProvider.withConfig(length = 56, prefix = "other.description")

  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>
      val preparedForm = request.userAnswers.get(OtherAssetDescriptionPage(index)) match {
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
          val answers = request.userAnswers.set(OtherAssetDescriptionPage(index), value)
          for {
            updatedAnswers <- Future.fromTry(answers)
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(OtherAssetDescriptionPage(index), mode, updatedAnswers))
        }
      )
  }
}
