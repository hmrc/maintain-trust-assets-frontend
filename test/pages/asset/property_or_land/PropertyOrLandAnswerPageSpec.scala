/*
 * Copyright 2026 HM Revenue & Customs
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

package pages.asset.property_or_land

import base.SpecBase
import pages.asset.property_or_land.add.PropertyOrLandAnswerPage
import play.api.libs.json.{JsPath, Json}

class PropertyOrLandAnswerPageSpec extends SpecBase {

  "PropertyOrLandAnswerPage" must {

    "return the correct JsPath for a given index" in {
      val index = 0
      val page  = PropertyOrLandAnswerPage(index)

      page.path mustBe (JsPath \ "assets" \ "property_or_land" \ index \ "answersComplete")
    }

    "produce distinct paths for different indexes" in {
      val firstPage  = PropertyOrLandAnswerPage(0)
      val secondPage = PropertyOrLandAnswerPage(1)

      firstPage.path must not be secondPage.path
    }

    "read the boolean value at the path from JSON" in {
      val index = 1
      val page  = PropertyOrLandAnswerPage(index)

      val json = Json.obj(
        "assets" -> Json.obj(
          "property_or_land" -> Json.arr(
            Json.obj("answersComplete" -> true),
            Json.obj("answersComplete" -> false)
          )
        )
      )

      val result = json.validate(page.path.read[Boolean]).get
      result mustBe false
    }
  }

}
