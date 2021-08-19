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
import controllers.actions.noneeabusiness.NameRequiredAction
import forms.CountryFormProvider
import models.Mode
import navigation.Navigator
import pages.asset.noneeabusiness.GoverningCountryPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptions
import views.html.asset.noneeabusiness.GoverningCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoverningCountryController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            standardActionSets: StandardActionSets,
                                            nameAction: NameRequiredAction,
                                            repository: PlaybackRepository,
                                            @NonEeaBusiness navigator: Navigator,
                                            countryFormProvider: CountryFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: GoverningCountryView,
                                            countryOptions: CountryOptions
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[String] = countryFormProvider.withPrefix("nonEeaBusiness.governingCountry")

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>

      val preparedForm = request.userAnswers.get(GoverningCountryPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, countryOptions.options(), mode, request.name))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, countryOptions.options(), mode, request.name))),

        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(GoverningCountryPage, value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(GoverningCountryPage, mode, updatedAnswers))
        }
      )
  }
}
