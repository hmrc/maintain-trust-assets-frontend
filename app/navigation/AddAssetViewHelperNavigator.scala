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

package navigation

import models.{LinearPageInJourney, NonLinearPageInJourney, PageInJourney}
import pages.asset.WhatKindOfAssetPage
import pages.asset.money._
import pages.asset.property_or_land._
import pages.asset.shares._
import pages.asset.business._
import pages.asset.partnership._
import pages.asset.other._

class AddAssetViewHelperNavigator {

  def money(index: Int): List[PageInJourney] = List(
    LinearPageInJourney(page = WhatKindOfAssetPage(index)),
    LinearPageInJourney(page = AssetMoneyValuePage(index))
  )

  def propertyOrLand(index: Int): List[PageInJourney] = List(
    LinearPageInJourney(page = WhatKindOfAssetPage(index)),
    NonLinearPageInJourney(
      page = PropertyOrLandAddressYesNoPage(index),
      yesPages = List(
        NonLinearPageInJourney(
          page = PropertyOrLandAddressUkYesNoPage(index),
          yesPages = List(LinearPageInJourney(PropertyOrLandUKAddressPage(index))),
          noPages = List(LinearPageInJourney(PropertyOrLandInternationalAddressPage(index)))
        )
      ),
      noPages = List(LinearPageInJourney(PropertyOrLandDescriptionPage(index)))
    ),
    LinearPageInJourney(page = PropertyOrLandTotalValuePage(index)),
    NonLinearPageInJourney(
      page = TrustOwnAllThePropertyOrLandPage(index),
      yesPages = Nil,
      noPages = List(LinearPageInJourney(PropertyLandValueTrustPage(index)))
    )
  )

  def shares(index: Int): List[PageInJourney] = List(
    LinearPageInJourney(page = WhatKindOfAssetPage(index)),
    NonLinearPageInJourney(
      page = SharesInAPortfolioPage(index),
      yesPages = List(
        LinearPageInJourney(page = SharePortfolioNamePage(index)),
        LinearPageInJourney(page = SharePortfolioOnStockExchangePage(index)),
        LinearPageInJourney(page = SharePortfolioQuantityInTrustPage(index)),
        LinearPageInJourney(page = SharePortfolioValueInTrustPage(index))
      ),
      noPages = List(
        LinearPageInJourney(page = ShareCompanyNamePage(index)),
        LinearPageInJourney(page = SharesOnStockExchangePage(index)),
        LinearPageInJourney(page = ShareClassPage(index)),
        LinearPageInJourney(page = ShareQuantityInTrustPage(index)),
        LinearPageInJourney(page = ShareValueInTrustPage(index))
      )
    )
  )

  def business(index: Int): List[PageInJourney] = List(
    LinearPageInJourney(page = WhatKindOfAssetPage(index)),
    LinearPageInJourney(page = BusinessNamePage(index)),
    LinearPageInJourney(page = BusinessDescriptionPage(index)),
    NonLinearPageInJourney(
      page = BusinessAddressUkYesNoPage(index),
      yesPages = List(LinearPageInJourney(BusinessUkAddressPage(index))),
      noPages = List(LinearPageInJourney(BusinessInternationalAddressPage(index)))
    ),
    LinearPageInJourney(page = BusinessValuePage(index))
  )

  def partnership(index: Int): List[PageInJourney] = List(
    LinearPageInJourney(page = WhatKindOfAssetPage(index)),
    LinearPageInJourney(page = PartnershipDescriptionPage(index)),
    LinearPageInJourney(page = PartnershipStartDatePage(index))
  )
  
  def other(index: Int): List[PageInJourney] = List(
    LinearPageInJourney(page = WhatKindOfAssetPage(index)),
    LinearPageInJourney(page = OtherAssetDescriptionPage(index)),
    LinearPageInJourney(page = OtherAssetValuePage(index))
  )

}
