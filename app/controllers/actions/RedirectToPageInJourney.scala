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

package controllers.actions

import com.google.inject.Inject
import models.{PageInJourney, LinearPageInJourney, UserAnswers, NonLinearPageInJourney}
import play.api.libs.json.{JsFalse, JsTrue}
import play.api.mvc.Call

class RedirectToPageInJourney @Inject()(userAnswers: UserAnswers, draftId: String) {

  def redirect[A](cya: Call, pages: List[PageInJourney]): Call = {
    @scala.annotation.tailrec
    def rec(pagesInJourney: List[PageInJourney]): Call = pagesInJourney match {
      case Nil => cya
      case _ => pagesInJourney.head match {
        case pageInJourney: LinearPageInJourney[A] =>
          userAnswers.getAtPath(pageInJourney.page.path) match {
            case Some(_) => rec(pagesInJourney.tail)
            case _ => pageInJourney.page.route(draftId)
          }
        case pageInJourney: NonLinearPageInJourney =>
          userAnswers.getAtPath(pageInJourney.page.path) match {
            case Some(JsTrue) =>
              rec(pageInJourney.yesPages ++ pagesInJourney.tail)
            case Some(JsFalse) =>
              rec(pageInJourney.noPages ++ pagesInJourney.tail)
            case _ => pageInJourney.page.route(draftId)
          }
      }
    }
    rec(pages)
  }
}
