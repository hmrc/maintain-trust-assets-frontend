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

package generators

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDate

trait ModelGenerators {

  implicit lazy val arbitraryWhatKindOfAsset: Arbitrary[WhatKindOfAsset] =
    Arbitrary {
      Gen.oneOf(WhatKindOfAsset.values)
    }

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        line1    <- arbitrary[String]
        line2    <- arbitrary[String]
        line3    <- arbitrary[String]
        line4    <- arbitrary[String]
        postcode <- arbitrary[String]
      } yield UkAddress(line1, line2, Some(line3), Some(line4), postcode)
    }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[NonUkAddress] =
    Arbitrary {
      for {
        str <- arbitrary[String]
      } yield NonUkAddress(str, str, Some(str), str)
    }

  implicit lazy val arbitraryShareClass: Arbitrary[ShareClass] =
    Arbitrary {
      Gen.oneOf(ShareClass.allValues)
    }

  implicit lazy val arbitraryAddAssets: Arbitrary[AddAssets] =
    Arbitrary {
      Gen.oneOf(AddAssets.values)
    }

  implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] =
    Arbitrary {
      for {
        year  <- Gen.choose(min = 1500, max = 2099)
        month <- Gen.choose(1, 12)
        day   <- Gen.choose(
                   min = 1,
                   max = month match {
                     case 2 if year % 4 == 0 => 29
                     case 2              => 28
                     case 4 | 6 | 9 | 11 => 30
                     case _              => 31
                   }
                 )
      } yield LocalDate.of(year, month, day)
    }

}
