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
import controllers.actions.{DraftIdRetrievalActionProvider, RegistrationDataRequiredAction, RegistrationIdentifierAction}
import controllers.filters.IndexActionFilterProvider
import forms.ValueFormProvider
import models.requests.RegistrationDataRequest
import navigation.Navigator
import pages.asset.property_or_land.{PropertyLandValueTrustPage, PropertyOrLandTotalValuePage}
import play.api.Logger
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import repositories.RegistrationsRepository
import uk.gov.hmrc.play.bootstrap.controller.FrontendBaseController
import views.html.asset.property_or_land.PropertyLandValueTrustView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertyLandValueTrustController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  repository: RegistrationsRepository,
                                                  @PropertyOrLand navigator: Navigator,
                                                  identify: RegistrationIdentifierAction,
                                                  getData: DraftIdRetrievalActionProvider,
                                                  requireData: RegistrationDataRequiredAction,
                                                  validateIndex: IndexActionFilterProvider,
                                                  formProvider: ValueFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: PropertyLandValueTrustView
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val logger: Logger = Logger(getClass)

  private val prefix: String = "propertyOrLand.valueInTrust"

  private def actions(index: Int, draftId: String): ActionBuilder[RegistrationDataRequest, AnyContent] =
    identify andThen
      getData(draftId) andThen
      requireData andThen
      validateIndex(index, sections.Assets)

  def onPageLoad(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId) {
    implicit request =>

      totalValue(index, draftId) match {
        case Left(value) =>
          val form = formProvider.withConfig(prefix, value)

          val preparedForm = request.userAnswers.get(PropertyLandValueTrustPage(index)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, index, draftId))
        case Right(redirect) =>
          redirect
      }
  }

  def onSubmit(index: Int, draftId: String): Action[AnyContent] = actions(index, draftId).async {
    implicit request =>

      totalValue(index, draftId) match {
        case Left(value) =>
          val form = formProvider.withConfig(prefix, value)

          form.bindFromRequest().fold(
            (formWithErrors: Form[_]) =>
              Future.successful(BadRequest(view(formWithErrors, index, draftId))),

            value => {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PropertyLandValueTrustPage(index), value))
                _              <- repository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(PropertyLandValueTrustPage(index), draftId)(updatedAnswers))
            }
          )
        case Right(redirect) =>
          Future.successful(redirect)
      }
  }

  private def totalValue(index: Int, draftId: String)(implicit request: RegistrationDataRequest[AnyContent]): Either[Long, Result] = {
    request.userAnswers.get(PropertyOrLandTotalValuePage(index)) match {
      case Some(value) =>
        Left(value)
      case _ =>
        logger.info(s"[totalValue][Session ID: ${request.sessionId}] Total value not found. Redirecting to total value page.")
        Right(Redirect(routes.PropertyOrLandTotalValueController.onPageLoad(index, draftId)))
    }
  }
}
