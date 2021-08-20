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
import navigation.AssetsNavigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.TrustService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.asset.nonTaxableToTaxable.{AssetInterruptView => MigrationInteruptPage}
import views.html.asset.noneeabusiness.AssetInterruptView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AssetInterruptPageController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              standardActionSets: StandardActionSets,
                                              repository: PlaybackRepository,
                                              trustService: TrustService,
                                              navigator: AssetsNavigator,
                                              val controllerComponents: MessagesControllerComponents,
                                              assetInterruptView: AssetInterruptView,
                                              migrationAssetInterruptView: MigrationInteruptPage
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = standardActionSets.verifiedForIdentifier {
    implicit request =>

      Ok(
      if (request.userAnswers.isMigratingToTaxable) {
        migrationAssetInterruptView()
      } else {
        assetInterruptView()
      }
    )
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.cleanup)
        _ <- repository.set(request.userAnswers)
        assets <- trustService.getAssets(updatedAnswers.identifier)
      } yield {
        Redirect(navigator.redirectFromInterruptPage(updatedAnswers.isMigratingToTaxable, assets.isEmpty))
      }
  }

}
