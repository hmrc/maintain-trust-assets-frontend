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

package controllers.asset.money

import controllers.actions._
import controllers.asset.RemoveAssetController
import forms.RemoveIndexFormProvider
import javax.inject.Inject
import models.requests.RegistrationDataRequest
import pages.QuestionPage
import pages.asset.money.AssetMoneyValuePage
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{ActionBuilder, AnyContent, Call, MessagesControllerComponents}
import repositories.RegistrationsRepository
import views.html.RemoveIndexView

class RemoveMoneyAssetController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            override val repository: RegistrationsRepository,
                                            override val formProvider: RemoveIndexFormProvider,
                                            identify: RegistrationIdentifierAction,
                                            getData: DraftIdRetrievalActionProvider,
                                            requireData: RegistrationDataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            require: RequiredAnswerActionProvider,
                                            val removeView: RemoveIndexView
                                           ) extends RemoveAssetController {

  override def page(index: Int): QuestionPage[String] = AssetMoneyValuePage(index)

  override val messagesPrefix : String = "removeMoneyAsset"

  override def actions(draftId : String, index: Int): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen getData(draftId) andThen requireData

  override def content(index: Int)(implicit request: RegistrationDataRequest[AnyContent]) : String =
    request.userAnswers.get(page(index)).map(x => s"£$x")
      .getOrElse(Messages(s"$messagesPrefix.default"))

  override def formRoute(draftId: String, index: Int): Call =
    routes.RemoveMoneyAssetController.onSubmit(index, draftId)
}


