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

package controllers.asset.property_or_land

import config.annotations.PropertyOrLand
import controllers.actions._
import models.Status.Completed
import navigation.Navigator
import pages.AssetStatus
import pages.asset.property_or_land.PropertyOrLandAnswerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.print.PropertyOrLandPrintHelper
import views.html.asset.property_or_land.PropertyOrLandAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertyOrLandAnswerController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                repository: RegistrationsRepository,
                                                @PropertyOrLand navigator: Navigator,
                                                actions: Actions,
                                                view: PropertyOrLandAnswersView,
                                                val controllerComponents: MessagesControllerComponents,
                                                printHelper: PropertyOrLandPrintHelper
                                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions.authWithData(draftId) {
    implicit request =>

      val sections = printHelper.checkDetailsSection(
        userAnswers = request.userAnswers,
        index = index,
        draftId = draftId
      )

      Ok(view(index, draftId, sections))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions.authWithData(draftId).async {
    implicit request =>

      val answers = request.userAnswers.set(AssetStatus(index), Completed)

      for {
        updatedAnswers <- Future.fromTry(answers)
        _ <- repository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(PropertyOrLandAnswerPage, draftId)(request.userAnswers))

  }
}
