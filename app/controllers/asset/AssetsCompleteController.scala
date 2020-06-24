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

import config.FrontendAppConfig
import controllers.actions.RegistrationIdentifierAction
import javax.inject.Inject
import mapping.AssetMapper
import models.Assets
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.SessionExpiredView

import scala.concurrent.{ExecutionContext, Future}

class AssetsCompleteController @Inject()(
                                          val controllerComponents: MessagesControllerComponents,
                                          view: SessionExpiredView,
                                          registrationsRepository: RegistrationsRepository,
                                          assetMapper: AssetMapper,
                                          config: FrontendAppConfig,
                                          identify: RegistrationIdentifierAction
                                        ) extends FrontendBaseController with I18nSupport {

  implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def onPageLoad(draftId: String): Action[AnyContent] = identify.async {
    implicit request =>
      registrationsRepository.get(draftId) map {
        case Some(userAnswers) =>
          assetMapper.build(userAnswers) match {
            case Some(assets) =>
              val json = Json.toJson(assets)
              println(s"Mapped json is => ${json}")
              Redirect(config.registrationProgressUrl(draftId))
            case _ => InternalServerError
          }
        case _ => Redirect(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
  }
}
