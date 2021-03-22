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

package controllers.asset

import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import controllers.filters.IndexActionFilterProvider
import forms.YesNoFormProvider
import models.UserAnswers
import models.requests.RegistrationDataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc._
import repositories.RegistrationsRepository
import sections.Assets
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels._
import views.html.asset.RemoveAssetYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveAssetYesNoController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            repository: RegistrationsRepository,
                                            identify: RegistrationIdentifierAction,
                                            getData: DraftIdRetrievalActionProvider,
                                            requireData: RegistrationDataRequiredAction,
                                            yesNoFormProvider: YesNoFormProvider,
                                            validateIndex: IndexActionFilterProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: RemoveAssetYesNoView
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form(prefix: String): Form[Boolean] = yesNoFormProvider.withPrefix(s"$prefix.removeYesNo")

  private def redirect(): Result = Redirect(controllers.asset.routes.AddAssetsController.onPageLoad())

  private def actions(index: Int): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen getData() andThen requireData andThen validateIndex(index, sections.Assets)

  def onPageLoad(index: Int): Action[AnyContent] = actions(index) {
    implicit request =>

      val prefix = determinePrefix(request.userAnswers.isTaxable)

      Ok(view(form(prefix), index, prefix, assetLabel(request.userAnswers, index)))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions(index).async {
    implicit request =>

      val prefix = determinePrefix(request.userAnswers.isTaxable)

      form(prefix).bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, index, prefix, assetLabel(request.userAnswers, index)))),

        remove => {
          if (remove) {
            for {
              updatedAnswers <- Future.fromTry(
                request.userAnswers.deleteAtPath(Assets.path \ index)
              )
              _ <- repository.set(updatedAnswers)
            } yield {
              redirect()
            }
          } else {
            Future.successful(redirect())
          }
        }
      )
  }

  private def assetLabel(userAnswers: UserAnswers, index: Int)
                        (implicit request: RegistrationDataRequest[AnyContent]): String = {

    def default(prefix: String = defaultPrefix): String = request.messages(messagesApi)(s"$prefix.defaultText")

    val path: JsPath = JsPath \ Assets \ index

    (for {
      pick <- userAnswers.data.transform(path.json.pick)
      asset <- pick.validate[AssetViewModel]
    } yield {
      asset match {
        case _: NonEeaBusinessAssetViewModel =>
          asset.label.getOrElse(default(determinePrefix(userAnswers.isTaxable)))
        case _ =>
          asset.label.getOrElse(default())
      }
    }).getOrElse(default())
  }

  private def determinePrefix(isTaxable: Boolean): String = {
    defaultPrefix + (if (!isTaxable) ".nonTaxable" else "")
  }

  private val defaultPrefix: String = "assets"

}
