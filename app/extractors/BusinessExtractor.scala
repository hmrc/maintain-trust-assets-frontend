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

package extractors

import models.assets.BusinessAssetType
import models.{NonUkAddress, UkAddress, UserAnswers}
import pages.QuestionPage
import pages.asset.business._
import pages.asset.business.amend.IndexPage
import play.api.libs.json.JsPath

import scala.util.Try

class BusinessExtractor extends AssetExtractor[BusinessAssetType] {

  override def apply(answers: UserAnswers,
                     assetType: BusinessAssetType,
                     index: Int): Try[UserAnswers] = {

    super.apply(answers, assetType, index)
      .flatMap(_.set(BusinessNamePage(index), assetType.orgName))
      .flatMap(_.set(BusinessDescriptionPage(index), assetType.businessDescription))
      .flatMap(answers => extractAddress(Some(assetType.address), answers))
      .flatMap(_.set(BusinessValuePage(index), assetType.businessValue))
  }

  override def ukAddressYesNoPage: QuestionPage[Boolean] = BusinessAddressUkYesNoPage(0)
  override def ukAddressPage: QuestionPage[UkAddress] = BusinessUkAddressPage(0)
  override def nonUkAddressPage: QuestionPage[NonUkAddress] = BusinessInternationalAddressPage(0)

  override def indexPage: QuestionPage[Int] = IndexPage

  override def basePath: JsPath = pages.asset.business.basePath
}
