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

  private val form: Form[Boolean] = yesNoFormProvider.withPrefix("assets.removeYesNo")

  private def redirect(draftId: String): Result = Redirect(controllers.asset.routes.AddAssetsController.onPageLoad(draftId))

  private def actions(index: Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen getData(draftId) andThen requireData andThen validateIndex(index, sections.Assets)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      Ok(view(form, draftId, index, assetLabel(request.userAnswers, index)))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId, index, assetLabel(request.userAnswers, index)))),

        remove => {
          if (remove) {
            for {
              updatedAnswers <- Future.fromTry(
                request.userAnswers.deleteAtPath(Assets.path \ index)
              )
              _ <- repository.set(updatedAnswers)
            } yield {
              redirect(draftId)
            }
          } else {
            Future.successful(redirect(draftId))
          }
        }
      )
  }

  private def assetLabel(userAnswers: UserAnswers, index: Int)
                        (implicit request: RegistrationDataRequest[AnyContent]): String = {

    def default(prefix: String = "assets"): String = request.messages(messagesApi)(s"$prefix.defaultText")

    val path: JsPath = JsPath \ Assets \ index

    (for {
      pick <- userAnswers.data.transform(path.json.pick)
      asset <- pick.validate[AssetViewModel]
    } yield {
      asset match {
        case _: NonEeaBusinessAssetViewModel =>
          asset.label.getOrElse {
            if (!userAnswers.isTaxable) default("assets.nonTaxable") else default()
          }
        case _ =>
          asset.label.getOrElse(default())
      }
    }).getOrElse(default())
  }

}
