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

package controllers.asset.property_or_land

import config.annotations.PropertyOrLand
import controllers.actions.StandardActionSets
import forms.ValueFormProvider
import models.requests.DataRequest
import navigation.Navigator
import pages.asset.property_or_land.{PropertyLandValueTrustPage, PropertyOrLandTotalValuePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.property_or_land.PropertyOrLandTotalValueView
import javax.inject.Inject
import models.Mode

import scala.concurrent.{ExecutionContext, Future}

class PropertyOrLandTotalValueController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    standardActionSets: StandardActionSets,
                                                    repository: PlaybackRepository,
                                                    @PropertyOrLand navigator: Navigator,
                                                    formProvider: ValueFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: PropertyOrLandTotalValueView
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>
      val form: Form[Long] = configuredForm(index)
      val preparedForm = request.userAnswers.get(PropertyOrLandTotalValuePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, index, mode))
  }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      val form: Form[Long] = configuredForm(index)
      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, index, mode))),
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PropertyOrLandTotalValuePage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PropertyOrLandTotalValuePage(index), mode, updatedAnswers))
        }
      )
  }

  private def configuredForm(index: Int)(implicit request: DataRequest[AnyContent]): Form[Long] = {
    formProvider.withConfig(
      prefix = "propertyOrLand.totalValue",
      minValue = request.userAnswers.get(PropertyLandValueTrustPage(index))
    )
  }
}
