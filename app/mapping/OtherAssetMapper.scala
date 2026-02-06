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

package mapping

import models.UserAnswers
import models.assets.OtherAssetType
import pages.asset.other.{OtherAssetDescriptionPage, OtherAssetValuePage}
import play.api.Logging
import play.api.libs.json.Reads
import play.api.libs.functional.syntax._

class OtherAssetMapper extends Mapper[OtherAssetType] with Logging {

  def apply(answers: UserAnswers): Option[OtherAssetType] = {
    val readFromUserAnswers: Reads[OtherAssetType] =
      (
        OtherAssetDescriptionPage(0).path.read[String] and
          OtherAssetValuePage(0).path.read[Long]
      )(OtherAssetType.apply _)

    mapAnswersWithExplicitReads(answers, readFromUserAnswers)
  }

}
