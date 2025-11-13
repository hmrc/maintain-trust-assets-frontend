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

package utils

import base.SpecBase
import controllers.asset._
import models.assets._
import models.{NonUkAddress, ShareClass}
import utils.Constants.QUOTED
import viewmodels.AddRow

import java.time.LocalDate

class AddAssetViewHelperSpec extends SpecBase {

  val viewHelper: AddAssetViewHelper = injector.instanceOf[AddAssetViewHelper]

  "AddAssetViewHelper" when {

    ".row" must {
      val nonEeaAsset1 = NonEeaBusinessType(None, "Non-EEA Business Name1", NonUkAddress("", "", None, ""), "", LocalDate.now, None, provisional = true)
      val nonEeaAsset2 = NonEeaBusinessType(None, "Non-EEA Business Name2", NonUkAddress("", "", None, ""), "", LocalDate.now, None, provisional = true)
      val moneyAsset = AssetMonetaryAmount(4000)
      val propertyOrLandAsset1 = PropertyLandType(Some("PropertyOrLand Name1"), None, 12L, None)
      val propertyOrLandAsset2 = PropertyLandType(Some("PropertyOrLand Name2"), None, 12L, None)
      val partnershipAsset1 = PartnershipType("Partnership Name1", LocalDate.now())
      val partnershipAsset2 = PartnershipType("Partnership Name2", LocalDate.now())
      val businessAsset1 = BusinessAssetType("Business Name1", "", NonUkAddress("", "", None, ""), 12L)
      val businessAsset2 = BusinessAssetType("Business Name2", "", NonUkAddress("", "", None, ""), 12L)
      val shares1 = SharesType("5", "Shares Name1", ShareClass.Other.toString, QUOTED, 12L)
      val shares2 = SharesType("7", "Shares Name2", ShareClass.Other.toString, QUOTED, 12L)
      val other1 = OtherAssetType("Other Asset1", 4000)
      val other2 = OtherAssetType("Other Asset2", 4000)


      "generate Nil for no user answers" in {
        val assets = Assets(Nil, Nil, Nil, Nil, Nil, Nil, Nil)

        val rows = viewHelper.rows(assets, isNonTaxable = false)
        rows.complete mustBe Nil
      }

      "generate rows from user answers for non-eea assets" in {
        val assets = Assets(Nil, Nil, Nil, Nil, Nil, Nil, List(nonEeaAsset1, nonEeaAsset2))

        val rows = viewHelper.rows(assets, isNonTaxable = false)
        rows.complete mustBe List(
          AddRow("Non-EEA Business Name1", typeLabel = "Company outside UK and EEA (European and Economic Area)", changeNonEeaBusinessAssetRoute(0), removeNonEeaBusinessAssetRoute(0)),
          AddRow("Non-EEA Business Name2", typeLabel = "Company outside UK and EEA (European and Economic Area)", changeNonEeaBusinessAssetRoute(1), removeNonEeaBusinessAssetRoute(1))
        )
      }

      "generate rows from user answers for money assets" in {
        val assets = Assets(List(moneyAsset), Nil, Nil, Nil, Nil, Nil, Nil)

        val rows = viewHelper.rows(assets, isNonTaxable = false)
        rows.complete mustBe List(
          AddRow("£4000", typeLabel = "Money", changeMoneyAssetRoute(0), removeMoneyAssetRoute(0))
        )
      }

      "generate rows from user answers for property or land assets" in {
        val assets = Assets(Nil, List(propertyOrLandAsset1, propertyOrLandAsset2), Nil, Nil, Nil, Nil, Nil)

        val rows = viewHelper.rows(assets, isNonTaxable = false)
        rows.complete mustBe List(
          AddRow("PropertyOrLand Name1", typeLabel = "Property or land", changePropertyLandAssetRoute(0), removePropertyLandAssetRoute(0)),
          AddRow("PropertyOrLand Name2", typeLabel = "Property or land", changePropertyLandAssetRoute(1), removePropertyLandAssetRoute(1))
        )
      }

      "generate rows from user answers for partnership assets" in {
        val assets = Assets(Nil, Nil, Nil, Nil, List(partnershipAsset1, partnershipAsset2), Nil, Nil)

        val rows = viewHelper.rows(assets, isNonTaxable = false)
        rows.complete mustBe List(
          AddRow("Partnership Name1", typeLabel = "Partnership", changePartnershipAssetRoute(0), removePartnershipRoute(0)),
          AddRow("Partnership Name2", typeLabel = "Partnership", changePartnershipAssetRoute(1), removePartnershipRoute(1))
        )
      }

      "generate rows from user answers for business assets" in {
        val assets = Assets(Nil, Nil, Nil, List(businessAsset1, businessAsset2), Nil, Nil, Nil)

        val rows = viewHelper.rows(assets, isNonTaxable = false)
        rows.complete mustBe List(
          AddRow("Business Name1", typeLabel = "Business", changeBusinessAssetRoute(0), removeBusinessAssetRoute(0)),
          AddRow("Business Name2", typeLabel = "Business", changeBusinessAssetRoute(1), removeBusinessAssetRoute(1))
        )
      }


      "generate rows from user answers for shares" in {
        val assets = Assets(Nil, Nil, List(shares1, shares2), Nil, Nil, Nil, Nil)

        val rows = viewHelper.rows(assets, isNonTaxable = false)
        rows.complete mustBe List(
          AddRow("Shares Name1", typeLabel = "Share", changeSharesAssetRoute(0), removeSharesAssetRoute(0)),
          AddRow("Shares Name2", typeLabel = "Share", changeSharesAssetRoute(1), removeSharesAssetRoute(1))
        )
      }

      "generate rows from user answers for other assets" in {
        val assets = Assets(Nil, Nil, Nil, Nil, Nil, List(other1, other2), Nil)

        val rows = viewHelper.rows(assets, isNonTaxable = false)
        rows.complete mustBe List(
          AddRow("Other Asset1", typeLabel = "Other", changeOtherAssetRoute(0), removeOtherAssetRoute(0)),
          AddRow("Other Asset2", typeLabel = "Other", changeOtherAssetRoute(1), removeOtherAssetRoute(1))
        )
      }

      "generate rows from user answers for complete assets" in {
        val assets = Assets(
          List(moneyAsset),
          List(propertyOrLandAsset1, propertyOrLandAsset2),
          List(shares1, shares2),
          List(businessAsset1, businessAsset2),
          List(partnershipAsset1, partnershipAsset2),
          List(other1, other2),
          List(nonEeaAsset1, nonEeaAsset2)
        )

        val rows = viewHelper.rows(assets, isNonTaxable = false)
        rows.complete mustBe List(
          AddRow("Non-EEA Business Name1", typeLabel = "Non-EEA Company", changeNonEeaBusinessAssetRoute(0), removeNonEeaBusinessAssetRoute(0)),
          AddRow("Non-EEA Business Name2", typeLabel = "Non-EEA Company", changeNonEeaBusinessAssetRoute(1), removeNonEeaBusinessAssetRoute(1)),
          AddRow("£4000", typeLabel = "Money", changeMoneyAssetRoute(0), removeMoneyAssetRoute(0)),
          AddRow("Non-EEA Business Name1", typeLabel = "Company outside UK and EEA (European and Economic Area)", changeNonEeaBusinessAssetRoute(0), removeNonEeaBusinessAssetRoute(0)),
          AddRow("Non-EEA Business Name2", typeLabel = "Company outside UK and EEA (European and Economic Area)", changeNonEeaBusinessAssetRoute(1), removeNonEeaBusinessAssetRoute(1)),
          AddRow("PropertyOrLand Name1", typeLabel = "Property or land", changePropertyLandAssetRoute(0), removePropertyLandAssetRoute(0)),
          AddRow("PropertyOrLand Name2", typeLabel = "Property or land", changePropertyLandAssetRoute(1), removePropertyLandAssetRoute(1)),
          AddRow("Other Asset1", typeLabel = "Other", changeOtherAssetRoute(0), removeOtherAssetRoute(0)),
          AddRow("Other Asset2", typeLabel = "Other", changeOtherAssetRoute(1), removeOtherAssetRoute(1)),
          AddRow("Business Name1", typeLabel = "Business", changeBusinessAssetRoute(0), removeBusinessAssetRoute(0)),
          AddRow("Business Name2", typeLabel = "Business", changeBusinessAssetRoute(1), removeBusinessAssetRoute(1)),
          AddRow("Partnership Name1", typeLabel = "Partnership", changePartnershipAssetRoute(0), removePartnershipRoute(0)),
          AddRow("Partnership Name2", typeLabel = "Partnership", changePartnershipAssetRoute(1), removePartnershipRoute(1)),
          AddRow("Shares Name1", typeLabel = "Share", changeSharesAssetRoute(0), removeSharesAssetRoute(0)),
          AddRow("Shares Name2", typeLabel = "Share", changeSharesAssetRoute(1), removeSharesAssetRoute(1))
        )
      }

      "only generate rows for non-EEA companies when non-taxable" in {
        val assets = Assets(
          List(moneyAsset),
          List(propertyOrLandAsset1, propertyOrLandAsset2),
          List(shares1, shares2),
          List(businessAsset1, businessAsset2),
          List(partnershipAsset1, partnershipAsset2),
          List(other1, other2),
          List(nonEeaAsset1, nonEeaAsset2)
        )

        val rows = viewHelper.rows(assets, isNonTaxable = true)
        rows.complete mustBe List(
          AddRow("Non-EEA Business Name1", typeLabel = "Company outside UK and EEA (European and Economic Area)", changeNonEeaBusinessAssetRoute(0), removeNonEeaBusinessAssetRoute(0)),
          AddRow("Non-EEA Business Name2", typeLabel = "Company outside UK and EEA (European and Economic Area)", changeNonEeaBusinessAssetRoute(1), removeNonEeaBusinessAssetRoute(1))
        )
      }
    }
  }

  def changeNonEeaBusinessAssetRoute(index: Int): String =
    noneeabusiness.amend.routes.AnswersController.extractAndRender(index).url

  def removeNonEeaBusinessAssetRoute(index: Int): String =
    noneeabusiness.remove.routes.RemoveAssetYesNoController.onPageLoad(index).url

  def changeMoneyAssetRoute(index: Int): String =
    controllers.asset.money.amend.routes.MoneyAmendAnswersController.extractAndRender(index).url

  def removeMoneyAssetRoute(index: Int): String =
    controllers.asset.money.remove.routes.RemoveAssetYesNoController.onPageLoad(index).url

  def changePropertyLandAssetRoute(index: Int): String =
    property_or_land.amend.routes.PropertyOrLandAmendAnswersController.extractAndRender(index).url

  def removePropertyLandAssetRoute(index: Int): String =
    property_or_land.remove.routes.PropertyOrLandRemoveAssetYesNoController.onPageLoad(index).url

  def changePartnershipAssetRoute(index: Int): String =
    partnership.amend.routes.PartnershipAmendAnswersController.extractAndRender(index).url

  def removePartnershipRoute(index: Int): String =
    partnership.remove.routes.RemovePartnershipAssetYesNoController.onPageLoad(index).url

  def changeBusinessAssetRoute(index: Int): String =
    business.amend.routes.BusinessAmendAnswersController.extractAndRender(index).url

  def removeBusinessAssetRoute(index: Int): String =
    business.remove.routes.RemoveBusinessAssetYesNoController.onPageLoad(index).url

  def changeSharesAssetRoute(index: Int): String =
    shares.amend.routes.ShareAmendAnswersController.extractAndRender(index).url

  def removeSharesAssetRoute(index: Int): String =
    shares.remove.routes.RemoveShareAssetYesNoController.onPageLoad(index).url

  def changeOtherAssetRoute(index: Int): String =
    other.amend.routes.AnswersController.extractAndRender(index).url

  def removeOtherAssetRoute(index: Int): String =
    other.remove.routes.RemoveAssetYesNoController.onPageLoad(index).url
}
