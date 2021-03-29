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

import config.annotations.Assets
import controllers.actions.StandardActionSets
import forms.WhatKindOfAssetFormProvider
import models.{Enumerable, NormalMode, UserAnswers, WhatKindOfAsset}
import navigation.Navigator
import pages.asset.WhatKindOfAssetPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.RadioOption
import views.html.asset.WhatKindOfAssetView
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class WhatKindOfAssetController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           standardActionSets: StandardActionSets,
                                           repository: PlaybackRepository,
                                           @Assets navigator: Navigator,
                                           formProvider: WhatKindOfAssetFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: WhatKindOfAssetView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  val form: Form[WhatKindOfAsset] = formProvider()

  private def options(userAnswers: UserAnswers): List[RadioOption] = {
    val assets = userAnswers.get(sections.Assets).getOrElse(Nil)
    val assetTypeSelected = userAnswers.get(WhatKindOfAssetPage)

    WhatKindOfAsset.nonMaxedOutOptions(assets, assetTypeSelected, userAnswers.is5mldEnabled)
  }

  def onPageLoad(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>
      val preparedForm = request.userAnswers.get(WhatKindOfAssetPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, index, options(request.userAnswers))) // TODO Index
  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      form.bindFromRequest().fold(
        (formWithErrors: Form[_]) =>
          Future.successful(BadRequest(view(formWithErrors, index, options(request.userAnswers)))), // TODO Index

        value => {

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatKindOfAssetPage, value))
            _ <- repository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhatKindOfAssetPage, NormalMode, updatedAnswers))
        }
      )
  }
}
