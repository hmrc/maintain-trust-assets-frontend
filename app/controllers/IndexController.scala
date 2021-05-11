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

package controllers

import connectors.TrustsConnector
import controllers.actions.StandardActionSets
import javax.inject.Inject
import models.UserAnswers
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.PlaybackRepository
import services.FeatureFlagService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 actions: StandardActionSets,
                                 cacheRepository : PlaybackRepository,
                                 connector: TrustsConnector,
                                 featureFlagService: FeatureFlagService
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(identifier: String): Action[AnyContent] =
    (actions.auth andThen actions.saveSession(identifier) andThen actions.getData).async {
    implicit request =>

      def redirect(userAnswers: UserAnswers): Result = {
          if (userAnswers.isMigratingToTaxable) {
            Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsYesNoController.onPageLoad())
          } else {
            Redirect(controllers.asset.noneeabusiness.routes.AddNonEeaBusinessAssetController.onPageLoad())
          }
        }


      logger.info(s"[Session ID: ${Session.id(hc)}][UTR/URN: $identifier]" +
        s" user has started to maintain assets")
      for {
        details <- connector.getTrustDetails(identifier)
        is5mldEnabled <- featureFlagService.is5mldEnabled()
        isMigrating <- connector.getTrustMigrationFlag(identifier)
        isUnderlyingData5mld <- connector.isTrust5mld(identifier)
        ua <- Future.successful(
          request.userAnswers.getOrElse {
            UserAnswers(
              internalId = request.user.internalId,
              identifier = identifier,
              whenTrustSetup = details.startDate,
              is5mldEnabled = is5mldEnabled,
              isTaxable = details.trustTaxable.getOrElse(true),
              isMigratingToTaxable = isMigrating.migratingFromNonTaxableToTaxable,
              isUnderlyingData5mld = isUnderlyingData5mld
            )
          }
        )
        _ <- cacheRepository.set(ua)
      } yield {
        redirect(ua)
      }
  }

}
