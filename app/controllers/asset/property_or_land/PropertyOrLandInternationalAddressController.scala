/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import controllers.filters.IndexActionFilterProvider
import forms.InternationalAddressFormProvider
import navigation.Navigator
import pages.asset.property_or_land.PropertyOrLandInternationalAddressPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.countryOptions.CountryOptionsNonUK
import views.html.asset.property_or_land.PropertyOrLandInternationalAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertyOrLandInternationalAddressController @Inject()(
                                                              override val messagesApi: MessagesApi,
                                                              repository: RegistrationsRepository,
                                                              @PropertyOrLand navigator: Navigator,
                                                              identify: RegistrationIdentifierAction,
                                                              getData: DraftIdRetrievalActionProvider,
                                                              requireData: RegistrationDataRequiredAction,
                                                              validateIndex: IndexActionFilterProvider,
                                                              formProvider: InternationalAddressFormProvider,
                                                              val controllerComponents: MessagesControllerComponents,
                                                              view: PropertyOrLandInternationalAddressView,
                                                              val countryOptions: CountryOptionsNonUK
                                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  private def actions(index: Int, draftId: String) =
      identify andThen
      getData(draftId) andThen
      requireData andThen
      validateIndex(index, sections.Assets)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val preparedForm = request.userAnswers.get(PropertyOrLandInternationalAddressPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, countryOptions.options, draftId, index))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, countryOptions.options, draftId, index))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PropertyOrLandInternationalAddressPage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(PropertyOrLandInternationalAddressPage(index), draftId)(updatedAnswers))
        }
      )
  }
}
