/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.actions.StandardActionSets
import forms.WhatKindOfAssetFormProvider
import models.WhatKindOfAsset.{Business, Money, NonEeaBusiness, Other, Partnership, PropertyOrLand, Shares}
import models.assets.Assets
import models.{Enumerable, Mode, NormalMode, WhatKindOfAsset}
import navigation.AssetsNavigator
import pages.asset.WhatKindOfAssetPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.RadioOption
import views.html.asset.WhatKindOfAssetView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatKindOfAssetController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           standardActionSets: StandardActionSets,
                                           repository: PlaybackRepository,
                                           navigator: AssetsNavigator,
                                           formProvider: WhatKindOfAssetFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: WhatKindOfAssetView,
                                           trustService: TrustService
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Enumerable.Implicits {

  val form: Form[WhatKindOfAsset] = formProvider()

  private def options(assets: models.assets.Assets): List[RadioOption] = {
    val kindsOfAsset = WhatKindOfAsset.nonMaxedOutOptions(assets).map(_.kindOfAsset)
    WhatKindOfAsset.options(kindsOfAsset)
  }

  def onPageLoad(index: Int, mode: Mode = NormalMode): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      for {
        assets: Assets <- trustService.getAssets(request.userAnswers.identifier)
      } yield {
        val preparedForm = request.userAnswers.get(WhatKindOfAssetPage(index)) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, index, options(assets)))
      }

  }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      trustService.getAssets(request.userAnswers.identifier).flatMap { assets: Assets =>
        form.bindFromRequest().fold(
          (formWithErrors: Form[_]) =>
            Future.successful(BadRequest(view(formWithErrors, index, options(assets)))),
          value => {
            val correctIndex = value match {
              case Money => assets.monetary.size
              case PropertyOrLand => assets.propertyOrLand.size
              case Shares => assets.shares.size
              case Business => assets.business.size
              case Partnership => assets.partnerShip.size
              case Other => assets.other.size
              case NonEeaBusiness => assets.nonEEABusiness.size
            }
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatKindOfAssetPage(correctIndex), value))
              _ <- repository.set(updatedAnswers)
            } yield Redirect(navigator.addAssetNowRoute(value, assets.partnerShip, Some(correctIndex)))
          }
        )
      }
  }
}
