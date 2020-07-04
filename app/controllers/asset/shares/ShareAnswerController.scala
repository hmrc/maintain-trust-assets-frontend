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

package controllers.asset.shares

import controllers.actions._
import controllers.filters.IndexActionFilterProvider
import javax.inject.Inject
import models.NormalMode
import models.Status.Completed
import navigation.Navigator
import pages.AssetStatus
import pages.asset.shares.{ShareAnswerPage, SharesInAPortfolioPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import utils.CheckYourAnswersHelper
import utils.annotations.Shares
import utils.countryOptions.CountryOptions
import viewmodels.AnswerSection
import views.html.asset.shares.ShareAnswersView

import scala.concurrent.{ExecutionContext, Future}

class ShareAnswerController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       repository: RegistrationsRepository,
                                       @Shares navigator: Navigator,
                                       identify: RegistrationIdentifierAction,
                                       getData: DraftIdRetrievalActionProvider,
                                       requireData: RegistrationDataRequiredAction,
                                       requiredAnswer: RequiredAnswerActionProvider,
                                       validateIndex: IndexActionFilterProvider,
                                       view: ShareAnswersView,
                                       countryOptions: CountryOptions,
                                       val controllerComponents: MessagesControllerComponents
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def actions(index: Int, draftId: String) =
    identify andThen
      getData(draftId) andThen
      requireData andThen
      requiredAnswer(RequiredAnswer(SharesInAPortfolioPage(index), routes.SharesInAPortfolioController.onPageLoad(NormalMode, index, draftId)))


  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      val answers = new CheckYourAnswersHelper(countryOptions)(request.userAnswers, draftId, canEdit = true)

      val sections = Seq(
        AnswerSection(
          None,
          request.userAnswers.get(SharesInAPortfolioPage(index)) match {
            case Some(false) =>
              Seq(
                answers.whatKindOfAsset(index),
                answers.sharesInAPortfolio(index),
                answers.shareCompanyName(index),
                answers.sharesOnStockExchange(index),
                answers.shareClass(index),
                answers.shareQuantityInTrust(index),
                answers.shareValueInTrust(index)
              ).flatten
            case Some(true) =>
              Seq(
                answers.whatKindOfAsset(index),
                answers.sharesInAPortfolio(index),
                answers.sharePortfolioName(index),
                answers.sharePortfolioOnStockExchange(index),
                answers.sharePortfolioQuantityInTrust(index),
                answers.sharePortfolioValueInTrust(index)
              ).flatten
            case None =>
              Nil
          }
        )
      )

      Ok(view(index, draftId, sections))
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      val answers = request.userAnswers.set(AssetStatus(index), Completed)

      for {
        updatedAnswers <- Future.fromTry(answers)
        _ <- repository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(ShareAnswerPage, NormalMode, draftId)(request.userAnswers))

  }
}
