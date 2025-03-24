/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.asset.money.add

import config.annotations.Money
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.money.NameRequiredAction
import handlers.ErrorHandler
import mapping.MoneyAssetMapper
import models.assets.AssetNameType
import models.{NormalMode, RemoveAsset}
import navigation.Navigator
import pages.asset.money.add.MoneyAnswerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.MoneyPrintHelper
import viewmodels.AnswerSection
import views.html.asset.money.MoneyAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MoneyAnswerController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       nameAction: NameRequiredAction,
                                       connector: TrustsConnector,
                                       @Money navigator: Navigator,
                                       view: MoneyAnswersView,
                                       val controllerComponents: MessagesControllerComponents,
                                       errorHandler: ErrorHandler,
                                       mapper: MoneyAssetMapper,
                                       printHelper: MoneyPrintHelper
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      println(request.userAnswers + "========================" + request.name)
      val section: AnswerSection = printHelper(userAnswers = request.userAnswers, provisional, request.name)
      Ok(view(section))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>

      mapper(request.userAnswers) match {
        case None =>
          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
        case Some(asset) =>
          connector.removeAsset(request.userAnswers.identifier, RemoveAsset(AssetNameType.MoneyAssetNameType, 0)).map(ele =>
            println(ele)
          )
          connector.addMoneyAsset(request.userAnswers.identifier, asset).map(_ =>
//            Redirect(navigator.nextPage(MoneyAnswerPage, NormalMode, request.userAnswers))
            Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
          )
      }
  }
}