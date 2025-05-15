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

package controllers.asset.partnership.add

import config.annotations.Partnership
import connectors.TrustsConnector
import controllers.actions._
import controllers.actions.partnership.NameRequiredAction
import controllers.asset.partnership.routes
import handlers.ErrorHandler
import mapping.PartnershipAssetMapper
import models.NormalMode
import models.Status.Completed
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.AssetStatus
import pages.asset.partnership.{PartnershipAnswerPage, PartnershipDescriptionPage, PartnershipStartDatePage}
import pages.asset.partnership.add.PartnershipAnswerPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, ActionBuilder, AnyContent, MessagesControllerComponents}
import repositories.PlaybackRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.print.PartnershipPrintHelper
import viewmodels.AnswerSection
import views.html.asset.partnership.PartnershipAnswersView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnershipAnswerController @Inject()(
                                             override val messagesApi: MessagesApi,
                                             standardActionSets: StandardActionSets,
                                             nameAction: NameRequiredAction,
                                             connector: TrustsConnector,
                                             repository: PlaybackRepository,
                                             identify: AuthenticatedIdentifierAction,
                                             getData: DraftIdDataRetrievalAction,
                                             requiredAnswer: RequiredAnswerActionProvider,
                                             requireData: RegistrationDataRequiredAction,
                                             @Partnership navigator: Navigator,
                                             view: PartnershipAnswersView,
                                             val controllerComponents: MessagesControllerComponents,
                                             errorHandler: ErrorHandler,
                                             mapper: PartnershipAssetMapper,
                                             printHelper: PartnershipPrintHelper
                                           )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

//  private val provisional: Boolean = true

  private def actions(index: Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen
      getData(draftId) andThen
      requireData andThen
      requiredAnswer(
        RequiredAnswer(
          PartnershipDescriptionPage(index),
          routes.PartnershipDescriptionController.onPageLoad(index, draftId)
        )
      ) andThen
      requiredAnswer(
        RequiredAnswer(
          PartnershipStartDatePage(index),
          routes.PartnershipStartDateController.onPageLoad(index, draftId)
        )
      )

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) { implicit request =>
    val sections = printHelper.checkDetailsSection(
      userAnswers = request.userAnswers,
      index = index,
      draftId = draftId
    )

    Ok(view(index, draftId, sections))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async { implicit request =>
    val answers = request.userAnswers.set(AssetStatus(index), Completed)

    for {
      updatedAnswers <- Future.fromTry(answers)
      _              <- repository.set(updatedAnswers)
    } yield Redirect(navigator.nextPage(PartnershipAnswerPage, draftId)(request.userAnswers))

  }

//  def onSubmit(): Action[AnyContent] = standardActionSets.verifiedForIdentifier.async {
//    implicit request =>
//      mapper(request.userAnswers) match {
//        case None =>
//          Future.successful(InternalServerError(errorHandler.internalServerErrorTemplate))
//        case Some(asset) =>
//          connector.getAssets(request.userAnswers.identifier).map {
//            case data =>
//              val matchFound = data.partnerShip.exists(ele =>
//                ele.description.equalsIgnoreCase(asset.description) &&
//                  ele.partnershipStart.equals(asset.partnershipStart)
//              )
//
//              if (!matchFound) {
//                connector.addPartnershipAsset(request.userAnswers.identifier, asset).map(_ =>
//                  Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad())
//                )
//              }
//          }
//          Future.successful(Redirect(controllers.asset.nonTaxableToTaxable.routes.AddAssetsController.onPageLoad()))
//      }
//  }
}
