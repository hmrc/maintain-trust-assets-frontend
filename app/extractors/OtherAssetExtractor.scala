/*
 * Copyright 2022 HM Revenue & Customs
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

package extractors

import models.UserAnswers
import models.assets.OtherAssetType
import pages.QuestionPage
import pages.asset.other.amend.IndexPage
import pages.asset.other.{OtherAssetDescriptionPage, OtherAssetValuePage}
import play.api.libs.json.JsPath

import scala.util.Try

class OtherAssetExtractor extends AssetExtractor[OtherAssetType] {

  override def apply(answers: UserAnswers,
                     otherAssetType: OtherAssetType,
                     index: Int): Try[UserAnswers] = {

    super.apply(answers, otherAssetType, index)
      .flatMap(_.set(OtherAssetDescriptionPage, otherAssetType.description))
      .flatMap(_.set(OtherAssetValuePage, otherAssetType.value))
  }

  override def indexPage: QuestionPage[Int] = IndexPage

  override def basePath: JsPath = pages.asset.other.basePath
}
