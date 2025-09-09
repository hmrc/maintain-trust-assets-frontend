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

package controllers.asset.shares.add

import config.annotations.Shares
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.shares.CompanyNameRequiredAction
import handlers.ErrorHandler
import mapping.ShareAssetMapper
import models.NormalMode
import models.requests.DataRequest
import navigation.Navigator
import pages.asset.shares.add.ShareAnswerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.SharesPrintHelper
import viewmodels.AnswerSection
import views.html.asset.shares.add.ShareAnswersView
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ShareAnswerController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       standardActionSets: StandardActionSets,
                                       @Shares navigator: Navigator,
                                       view: ShareAnswersView,
                                       val controllerComponents: MessagesControllerComponents,
                                       nameAction: CompanyNameRequiredAction,
                                       printHelper: SharesPrintHelper,
                                       connector: TrustsConnector,
                                       mapper: ShareAssetMapper,
                                       errorHandler: ErrorHandler,
                                       repository: PlaybackRepository
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val provisional: Boolean = true

  def onPageLoad(index: Int): Action[AnyContent] = (standardActionSets.verifiedForIdentifier andThen nameAction) {
    implicit request =>
      val section: AnswerSection = printHelper(request.userAnswers, index, provisional, request.name)
      Ok(view(index, section))
    }

  def onSubmit(index: Int): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
    implicit request =>
      mapper(request.userAnswers) match {
        case None => errorHandler.internalServerErrorTemplate.map(InternalServerError(_))
        case Some(asset) =>
          connector.amendSharesAsset(request.userAnswers.identifier, index, asset).flatMap { response =>
            response.status match {
              case OK | NO_CONTENT => cleanAllAndRedirect(index)
              case _ =>
                connector.getAssets(request.userAnswers.identifier).flatMap { data =>
                  val matchFound = data.shares.exists(ele =>
                    ele.orgName.equalsIgnoreCase(asset.orgName) &&
                      ele.isPortfolio == asset.isPortfolio &&
                      ele.shareClass.equalsIgnoreCase(asset.shareClass) &&
                      ele.typeOfShare.equalsIgnoreCase(asset.typeOfShare) &&
                      ele.numberOfShares.equalsIgnoreCase(asset.numberOfShares) &&
                      ele.shareClassDisplay == asset.shareClassDisplay &&
                      ele.value == asset.value
                  )
                  if (!matchFound) connector.addSharesAsset(request.userAnswers.identifier, asset).flatMap(_ => cleanAllAndRedirect(index))
                  else cleanAllAndRedirect(index)
                }
            }
          }
      }
    }

  private def cleanAllAndRedirect(index: Int)(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val next = navigator.nextPage(ShareAnswerPage(index), NormalMode, request.userAnswers)
    request.userAnswers.cleanupPreservingShares.fold(
      _ => Future.successful(Redirect(next)),
      cleanedUa => repository.set(cleanedUa).map(_ => Redirect(next))
    )
  }
}
