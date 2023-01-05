/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.asset.shares

import config.annotations.Shares
import controllers.actions.StandardActionSets
import forms.YesNoFormProvider
import navigation.Navigator
import pages.asset.shares.SharePortfolioOnStockExchangePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.shares.SharePortfolioOnStockExchangeView
import javax.inject.Inject
import models.Mode

import scala.concurrent.{ExecutionContext, Future}

class SharePortfolioOnStockExchangeController @Inject()(
                                                         override val messagesApi: MessagesApi,
                                                         standardActionSets: StandardActionSets,
                                                         repository: PlaybackRepository,
                                                         @Shares navigator: Navigator,
                                                         yesNoFormProvider: YesNoFormProvider,
                                                         val controllerComponents: MessagesControllerComponents,
                                                         view: SharePortfolioOnStockExchangeView
                                                       )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = yesNoFormProvider.withPrefix("shares.portfolioOnStockExchangeYesNo")

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(SharePortfolioOnStockExchangePage) match {
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
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(SharePortfolioOnStockExchangePage, value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(SharePortfolioOnStockExchangePage, mode, updatedAnswers))
        }
      )
  }
}
