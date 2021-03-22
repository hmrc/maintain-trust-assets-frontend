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

package controllers.asset.noneeabusiness

import config.annotations.NonEeaBusiness
import controllers.actions._
import controllers.filters.IndexActionFilterProvider
import forms.InternationalAddressFormProvider
import models.InternationalAddress
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.asset.noneeabusiness.{InternationalAddressPage, NamePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import sections.Assets
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptionsNonUK
import views.html.asset.noneeabusiness.InternationalAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InternationalAddressController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                registrationsRepository: RegistrationsRepository,
                                                @NonEeaBusiness navigator: Navigator,
                                                validateIndex: IndexActionFilterProvider,
                                                identify: RegistrationIdentifierAction,
                                                getData: DraftIdRetrievalActionProvider,
                                                requireData: RegistrationDataRequiredAction,
                                                requiredAnswer: RequiredAnswerActionProvider,
                                                formProvider: InternationalAddressFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: InternationalAddressView,
                                                val countryOptions: CountryOptionsNonUK
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[InternationalAddress] = formProvider()

  private def actions(index: Int): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen
      getData() andThen
      requireData andThen
      validateIndex(index, Assets) andThen
      requiredAnswer(RequiredAnswer(NamePage(index), routes.NameController.onPageLoad(index)))

  def onPageLoad(index: Int): Action[AnyContent] = actions(index) {
    implicit request =>

      val name = request.userAnswers.get(NamePage(index)).get

      val preparedForm = request.userAnswers.get(InternationalAddressPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, countryOptions.options, index, name))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions(index).async {
    implicit request =>

      val name = request.userAnswers.get(NamePage(index)).get

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, countryOptions.options, index, name))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(InternationalAddressPage(index), value))
            _ <- registrationsRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(InternationalAddressPage(index))(updatedAnswers))
        }
      )
  }
}
