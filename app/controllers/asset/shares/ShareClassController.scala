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

package controllers.asset.shares

import config.annotations.Shares
import controllers.actions._
import controllers.actions.shares.CompanyNameRequiredAction
import forms.shares.ShareClassFormProvider
import models.{Enumerable, Mode}
import navigation.Navigator
import pages.asset.shares.{ShareClassPage, ShareCompanyNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.shares.ShareClassView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ShareClassController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      standardActionSets: StandardActionSets,
                                      nameAction: CompanyNameRequiredAction,
                                      repository: PlaybackRepository,
                                      @Shares navigator: Navigator,
                                      formProvider: ShareClassFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: ShareClassView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] =
    (standardActionSets.verifiedForIdentifier andThen nameAction) { implicit request =>
      val preparedForm = request.userAnswers.get(ShareClassPage(index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      val companyName = request.userAnswers.get(ShareCompanyNamePage(index)).getOrElse("")
      Ok(view(preparedForm, index, mode, companyName))
    }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] =
    (standardActionSets.verifiedForIdentifier andThen nameAction).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val companyName = request.userAnswers.get(ShareCompanyNamePage(index)).getOrElse("")
          Future.successful(BadRequest(view(formWithErrors, index, mode, companyName)))
        },
        value => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ShareClassPage(index), value))
            _              <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ShareClassPage(index), mode, updatedAnswers))
        }
      )
    }
}
