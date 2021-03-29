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

import controllers.actions.StandardActionSets
import forms.AddAssetTypeFormProvider
import javax.inject.Inject
import models.WhatKindOfAsset._
import models.{NormalMode, WhatKindOfAsset}
import pages.asset.AddNowPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.AddNowView

import scala.concurrent.{ExecutionContext, Future}

class AddNowController @Inject()(
                                  override val messagesApi: MessagesApi,
                                  standardActionSets: StandardActionSets,
                                  val controllerComponents: MessagesControllerComponents,
                                  view: AddNowView,
                                  formProvider: AddAssetTypeFormProvider,
                                  repository: PlaybackRepository
                                  )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[WhatKindOfAsset] = formProvider()

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>

      val preparedForm = request.userAnswers.get(AddNowPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddNowPage, value))
            _ <- repository.set(updatedAnswers)
          } yield {
            value match {
              case Money => Redirect(controllers.asset.money.routes.AssetMoneyValueController.onPageLoad(NormalMode))
              case PropertyOrLand => Redirect(controllers.asset.property_or_land.routes.PropertyOrLandAddressYesNoController.onPageLoad(NormalMode))
              case Shares => Redirect(controllers.asset.shares.routes.SharesInAPortfolioController.onPageLoad(NormalMode))
              case Business => Redirect(controllers.asset.business.routes.BusinessNameController.onPageLoad(NormalMode))
              case Partnership => Redirect(controllers.asset.partnership.routes.PartnershipDescriptionController.onPageLoad(NormalMode))
              case Other => Redirect(controllers.asset.other.routes.OtherAssetDescriptionController.onPageLoad(NormalMode))
              case NonEeaBusiness => Redirect(controllers.asset.noneeabusiness.routes.NameController.onPageLoad(NormalMode))
            }
          }
      )
  }
}
