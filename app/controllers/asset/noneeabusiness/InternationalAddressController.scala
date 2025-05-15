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

package controllers.asset.noneeabusiness

import config.annotations.NonEeaBusiness
import controllers.actions._
import controllers.actions.noneeabusiness.NameRequiredAction
import controllers.filters.IndexActionFilterProvider
import forms.InternationalAddressFormProvider
import models.requests.DataRequest
import models.{Mode, NonUkAddress}
import navigation.Navigator
import pages.asset.noneeabusiness.{NamePage, NonUkAddressPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import sections.Assets
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptionsNonUK
import views.html.asset.noneeabusiness.InternationalAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InternationalAddressController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                standardActionSets: StandardActionSets,
                                                nameAction: NameRequiredAction,
                                                repository: PlaybackRepository,
                                                identify: AuthenticatedIdentifierAction,
                                                getData: DraftIdDataRetrievalAction,
                                                requiredAnswer: RequiredAnswerActionProvider,
                                                requireData: RegistrationDataRequiredAction,
                                                validateIndex: IndexActionFilterProvider,
                                                @NonEeaBusiness navigator: Navigator,
                                                formProvider: InternationalAddressFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: InternationalAddressView,
                                                val countryOptions: CountryOptionsNonUK
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[NonUkAddress] = formProvider()

  private def actions(index: Int, draftId: String): ActionBuilder[DataRequest, AnyContent] =
    identify andThen
      getData(draftId) andThen
      requireData andThen
      validateIndex(index, Assets) andThen
      requiredAnswer(RequiredAnswer(NamePage(index), routes.NameController.onPageLoad(index, draftId)))

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) { implicit request =>
    val name = request.userAnswers.get(NamePage(index)).get

    val preparedForm = request.userAnswers.get(NonUkAddressPage(index)) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, countryOptions.options(), index, draftId, name))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async { implicit request =>
    val name = request.userAnswers.get(NamePage(index)).get

    form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, countryOptions.options(), index, draftId, name))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(NonUkAddressPage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(NonUkAddressPage(index), draftId)(updatedAnswers))
      )
  }
}
