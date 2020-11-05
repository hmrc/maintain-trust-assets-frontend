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

package controllers.asset.property_or_land

import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import controllers.filters.IndexActionFilterProvider
import forms.ValueFormProvider
import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.asset.property_or_land.PropertyOrLandTotalValuePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.annotations.PropertyOrLand
import views.html.asset.property_or_land.PropertyOrLandTotalValueView

import scala.concurrent.{ExecutionContext, Future}

class PropertyOrLandTotalValueController @Inject()(
                                                    override val messagesApi: MessagesApi,
                                                    repository: RegistrationsRepository,
                                                    @PropertyOrLand navigator: Navigator,
                                                    identify: RegistrationIdentifierAction,
                                                    getData: DraftIdRetrievalActionProvider,
                                                    requireData: RegistrationDataRequiredAction,
                                                    validateIndex: IndexActionFilterProvider,
                                                    formProvider: ValueFormProvider,
                                                    val controllerComponents: MessagesControllerComponents,
                                                    view: PropertyOrLandTotalValueView
                                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider.withConfig("propertyOrLand.totalValue")

  private def actions(index: Int, draftId: String) =
    identify andThen
      getData(draftId) andThen
      requireData andThen
      validateIndex(index, sections.Assets)

  def onPageLoad(mode: Mode, index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PropertyOrLandTotalValuePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, index, draftId))
  }

  def onSubmit(mode: Mode, index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, index, draftId))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PropertyOrLandTotalValuePage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PropertyOrLandTotalValuePage(index), mode, draftId)(updatedAnswers))
        }
      )
  }
}
