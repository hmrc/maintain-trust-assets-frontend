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

package controllers

import connectors.TrustsConnector
import controllers.actions.StandardActionSets
import models.TaskStatus.InProgress
import models.UserAnswers
import navigation.AssetsNavigator
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import services.{TrustService, TrustsStoreService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Session

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 actions: StandardActionSets,
                                 cacheRepository: PlaybackRepository,
                                 connector: TrustsConnector,
                                 trustsStoreService: TrustsStoreService,
                                 trustService: TrustService,
                                 navigator: AssetsNavigator
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(identifier: String): Action[AnyContent] =
    (actions.auth andThen actions.saveSession(identifier) andThen actions.getData).async {
      implicit request =>

        logger.info(s"[Session ID: ${Session.id(hc)}][UTR/URN: $identifier]" +
          s" user has started to maintain assets")
        for {
          assets <- trustService.getAssets(identifier)

          details <- connector.getTrustDetails(identifier)
          is5mldEnabled <- trustsStoreService.is5mldEnabled()
          isMigrating <- connector.getTrustMigrationFlag(identifier)
          isUnderlyingData5mld <- connector.isTrust5mld(identifier)
          ua <- Future.successful(
            request.userAnswers.getOrElse {
              UserAnswers(
                internalId = request.user.internalId,
                identifier = identifier,
                sessionId = Session.id(hc),
                newId = s"${request.user.internalId}-$identifier-${Session.id(hc)}",
                whenTrustSetup = details.startDate,
                is5mldEnabled = is5mldEnabled,
                isTaxable = details.trustTaxable.getOrElse(true),
                isMigratingToTaxable = isMigrating.migratingFromNonTaxableToTaxable,
                isUnderlyingData5mld = isUnderlyingData5mld
              )
            }
          )
          _ <- cacheRepository.set(ua)
          _ <- trustsStoreService.updateTaskStatus(identifier, InProgress)
        } yield {

          println("request.userAnswers :::::::::::::::: "+assets +":::::::::::::: "+ assets.totalSizeCount)
          Redirect(navigator.redirectToAddAssetPage(ua.isMigratingToTaxable, Some(0)))
        }
    }

}
