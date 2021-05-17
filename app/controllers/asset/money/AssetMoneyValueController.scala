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

package controllers.asset.money

import config.annotations.Money
import connectors.TrustsConnector
import controllers.actions.StandardActionSets
import forms.ValueFormProvider
import models.Status.Completed
import navigation.Navigator
import pages.AssetStatus
import pages.asset.money.AssetMoneyValuePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.money.AssetMoneyValueView

import javax.inject.Inject
import models.assets.AssetMonetaryAmount
import models.{CheckMode, Mode}
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}

class AssetMoneyValueController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           standardActionSets: StandardActionSets,
                                           repository: PlaybackRepository,
                                           @Money navigator: Navigator,
                                           formProvider: ValueFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: AssetMoneyValueView,
                                           connector: TrustsConnector
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Long] = formProvider.withConfig(prefix = "money.value")

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(AssetMoneyValuePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {

          val answers = request.userAnswers.set(AssetMoneyValuePage, value)
            .flatMap(_.set(AssetStatus, Completed))

          val addOrAmendMoney = if(mode == CheckMode) {
            connector.amendMoneyAsset(request.userAnswers.identifier, 0, AssetMonetaryAmount(value))
          } else {
            connector.addMoneyAsset(request.userAnswers.identifier, AssetMonetaryAmount(value))
          }

          for {
            updatedAnswers <- Future.fromTry(answers)
            _              <- repository.set(updatedAnswers)
            _              <- addOrAmendMoney
          } yield Redirect(navigator.nextPage(AssetMoneyValuePage, mode, updatedAnswers))
          }
      )
  }
}
