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

package controllers

import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import controllers.filters.IndexActionFilterProvider
import forms.WhatKindOfAssetFormProvider
import javax.inject.Inject
import models.WhatKindOfAsset.Money
import models.requests.RegistrationDataRequest
import models.{Enumerable, Mode, WhatKindOfAsset}
import navigation.Navigator
import pages.asset.WhatKindOfAssetPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import viewmodels.{AssetViewModel, MoneyAssetViewModel}
import views.html.asset.WhatKindOfAssetView

import scala.concurrent.{ExecutionContext, Future}

class WhatKindOfAssetController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           registrationsRepository: RegistrationsRepository,
                                           navigator: Navigator,
                                           identify: RegistrationIdentifierAction,
                                           getData: DraftIdRetrievalActionProvider,
                                           requireData: RegistrationDataRequiredAction,
                                           validateIndex: IndexActionFilterProvider,
                                           formProvider: WhatKindOfAssetFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: WhatKindOfAssetView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  private val form = formProvider()

  private def findAssetThatIsMoney(assets : List[AssetViewModel]): Option[(AssetViewModel, Int)] =
    assets.zipWithIndex.find {_._1.isInstanceOf[MoneyAssetViewModel]}

  private def options(request : RegistrationDataRequest[AnyContent], index: Int) = {
    val assets = request.userAnswers.get(sections.Assets).getOrElse(Nil)
    
    findAssetThatIsMoney(assets) match {
      case Some((_, i)) if i == index =>
        WhatKindOfAsset.options
      case Some((_, i)) if i != index =>
        WhatKindOfAsset.options.filterNot(_.value == Money.toString)
      case _ =>
        WhatKindOfAsset.options
    }
  }

  private def actions (index: Int, draftId: String) =
    identify andThen getData(draftId) andThen requireData andThen validateIndex(index, sections.Assets)

  def onPageLoad(mode: Mode, index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>
      val preparedForm = request.userAnswers.get(WhatKindOfAssetPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, draftId, index, options(request, index)))
  }

  def onSubmit(mode: Mode, index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      val assets = request.userAnswers.get(sections.Assets).getOrElse(Nil)

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, mode, draftId, index, options(request, index)))),

        value => {

          def insertAndRedirect() =
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatKindOfAssetPage(index), value))
              _ <- registrationsRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhatKindOfAssetPage(index), mode, draftId)(updatedAnswers))

          value match {
            case Money =>
              findAssetThatIsMoney(assets) match {
                case Some((_ , i)) if i == index =>
                  insertAndRedirect()
                case Some((_, i)) if i != index =>
                  Future.successful(BadRequest(view(form.fill(Money), mode, draftId, index, options(request, index))))
                case _ => insertAndRedirect()
            }
            case _ =>
              insertAndRedirect()
          }
        }
      )
  }
}
