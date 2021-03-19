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
import navigation.Navigator
import pages.asset.AssetInterruptPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import views.html.asset.{NonTaxableInfoView, TaxableInfoView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AssetInterruptPageController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              repository: RegistrationsRepository,
                                              identify: RegistrationIdentifierAction,
                                              getData: DraftIdRetrievalActionProvider,
                                              requireData: RegistrationDataRequiredAction,
                                              navigator: Navigator,
                                              val controllerComponents: MessagesControllerComponents,
                                              taxableView: TaxableInfoView,
                                              nonTaxableView: NonTaxableInfoView
                                            )(implicit ec: ExecutionContext) extends AddAssetController {

  def onPageLoad(draftId: String): Action[AnyContent] = (identify andThen getData(draftId) andThen requireData) {
    implicit request =>
      Ok(
        if (request.userAnswers.isTaxable) {
          taxableView(draftId, request.userAnswers.is5mldEnabled)
        } else {
          nonTaxableView(draftId)
        }
      )
  }

  def onSubmit(draftId: String): Action[AnyContent] = (identify andThen getData(draftId) andThen requireData).async {
    implicit request =>
      for {
        updatedAnswers <- Future.fromTry(setAssetTypeIfNonTaxable(request.userAnswers, 0))
        _ <- repository.set(updatedAnswers)
      } yield {
        Redirect(navigator.nextPage(AssetInterruptPage, draftId)(updatedAnswers))
      }
  }

}
