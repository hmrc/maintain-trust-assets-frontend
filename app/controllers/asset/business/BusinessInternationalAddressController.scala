/*
 * Copyright 2026 HM Revenue & Customs
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
import forms.InternationalAddressFormProvider
import models.{Mode, NonUkAddress}
import navigation.Navigator
import pages.asset.business.{BusinessInternationalAddressPage, BusinessNamePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.countryOptions.CountryOptionsNonUK
import views.html.asset.business.BusinessInternationalAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessInternationalAddressController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  nameAction: NameRequiredAction,
  repository: PlaybackRepository,
  @Business navigator: Navigator,
  formProvider: InternationalAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: BusinessInternationalAddressView,
  val countryOptions: CountryOptionsNonUK
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form: Form[NonUkAddress] = formProvider()

  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>
      val preparedForm = request.userAnswers.get(BusinessInternationalAddressPage(index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      request.userAnswers.get(BusinessNamePage(index)) match {
        case Some(businessName) =>
          Ok(view(preparedForm, index, countryOptions.options(), mode, businessName))

        case None =>
          Redirect(controllers.asset.business.routes.BusinessNameController.onPageLoad(index, mode))
      }
  }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      request.userAnswers.get(BusinessNamePage(index)) match {
        case Some(businessName) =>
          form
            .bindFromRequest()
            .fold(
              (formWithErrors: Form[_]) =>
                Future
                  .successful(BadRequest(view(formWithErrors, index, countryOptions.options(), mode, businessName))),
              value =>
                for {
                  updatedAnswers <-
                    Future.fromTry(request.userAnswers.set(BusinessInternationalAddressPage(index), value))
                  _              <- repository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(BusinessInternationalAddressPage(index), mode, updatedAnswers))
            )

        case None =>
          Future.successful(Redirect(controllers.asset.business.routes.BusinessNameController.onPageLoad(index, mode)))
      }
  }

}
