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
import forms.WhatKindOfAssetFormProvider
import models.requests.RegistrationDataRequest
import models.{Enumerable, UserAnswers, WhatKindOfAsset}
import navigation.Navigator
import pages.asset.WhatKindOfAssetPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import viewmodels.RadioOption
import views.html.asset.WhatKindOfAssetView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatKindOfAssetController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           repository: RegistrationsRepository,
                                           navigator: Navigator,
                                           identify: RegistrationIdentifierAction,
                                           getData: DraftIdRetrievalActionProvider,
                                           requireData: RegistrationDataRequiredAction,
                                           validateIndex: IndexActionFilterProvider,
                                           formProvider: WhatKindOfAssetFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: WhatKindOfAssetView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  val form: Form[WhatKindOfAsset] = formProvider()

  private def options(userAnswers: UserAnswers, index: Int): List[RadioOption] = {
    val assets = userAnswers.get(sections.Assets).getOrElse(Nil)
    val assetTypeAtIndex = userAnswers.get(WhatKindOfAssetPage(index))

    WhatKindOfAsset.nonMaxedOutOptions(assets, assetTypeAtIndex)
  }

  private def actions (index: Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen getData(draftId) andThen requireData andThen validateIndex(index, sections.Assets)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(WhatKindOfAssetPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, draftId, index, options(request.userAnswers, index)))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, draftId, index, options(request.userAnswers, index)))),

        value => {

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatKindOfAssetPage(index), value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhatKindOfAssetPage(index), draftId)(updatedAnswers))
        }
      )
  }
}
