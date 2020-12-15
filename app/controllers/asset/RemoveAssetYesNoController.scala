/*
 * Copyright 2020 HM Revenue & Customs
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
import models.requests.RegistrationDataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsPath, JsValue}
import play.api.mvc._
import repositories.RegistrationsRepository
import sections.Assets
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.CheckAnswersFormatters
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
                                            view: RemoveAssetYesNoView,
                                            checkAnswersFormatters: CheckAnswersFormatters
                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[Boolean] = yesNoFormProvider.withPrefix("assets.removeYesNo")

  private def redirect(draftId: String): Result = Redirect(controllers.asset.routes.AddAssetsController.onPageLoad(draftId))

  private def actions(index: Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen getData(draftId) andThen requireData andThen validateIndex(index, sections.Assets)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      Ok(view(form, draftId, index, assetLabel(request.userAnswers.data, index)))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId, index, assetLabel(request.userAnswers.data, index)))),

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

  private def assetLabel(json: JsValue, index: Int)
                        (implicit request: RegistrationDataRequest[AnyContent]): String = {

    val default: String = request.messages(messagesApi)("assets.defaultText")

    def propertyOrLandLabel(propertyOrLand: PropertyOrLandAssetViewModel): String = {
      (propertyOrLand.address, propertyOrLand.description) match {
        case (Some(address), _) => address
        case (_, Some(description)) => description
        case _ => default
      }
    }

    val path: JsPath = JsPath \ Assets \ index

    (for {
      pick <- json.transform(path.json.pick)
      asset <- pick.validate[AssetViewModel]
    } yield {
      asset match {
        case money: MoneyAssetViewModel => money.value.fold(default)(checkAnswersFormatters.currencyFormat)
        case propertyOrLand: PropertyOrLandAssetViewModel => propertyOrLandLabel(propertyOrLand)
        case shares: ShareAssetViewModel => shares.name.getOrElse(default)
        case business: BusinessAssetViewModel => business.name.getOrElse(default)
        case partnership: PartnershipAssetViewModel => partnership.description.getOrElse(default)
        case other: OtherAssetViewModel => other.description.getOrElse(default)
        case _ => default
      }
    }).getOrElse(default)
  }

}
