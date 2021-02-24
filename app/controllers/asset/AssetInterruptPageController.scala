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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.{NonTaxableInfoView, TaxableInfoView}

import javax.inject.Inject

class AssetInterruptPageController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              identify: RegistrationIdentifierAction,
                                              getData: DraftIdRetrievalActionProvider,
                                              requireData: RegistrationDataRequiredAction,
                                              val controllerComponents: MessagesControllerComponents,
                                              taxableView: TaxableInfoView,
                                              nonTaxableView: NonTaxableInfoView
                                            ) extends FrontendBaseController with I18nSupport {

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

  def onSubmit(draftId: String): Action[AnyContent] = (identify andThen getData(draftId) andThen requireData) {
    Redirect(routes.WhatKindOfAssetController.onPageLoad(0, draftId))
  }

}
