/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import models.ShareClass._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ShareClassSpec extends AnyWordSpec with Matchers {

  "ShareClass" must {
    "return the correct value for toDES " in {
      ShareClass.toDES(Ordinary) mustBe "Ordinary shares"
      ShareClass.toDES(NonVoting) mustBe "Non-voting shares"
      ShareClass.toDES(Redeemable) mustBe "Redeemable shares"
      ShareClass.toDES(Preference) mustBe "Preference shares"
      ShareClass.toDES(Deferred) mustBe "Deferred ordinary shares"
      ShareClass.toDES(Management) mustBe "Management shares"
      ShareClass.toDES(OtherClasses) mustBe "Other classes of shares"
      ShareClass.toDES(Voting) mustBe "Voting shares"
      ShareClass.toDES(Dividend) mustBe "Dividend shares"
      ShareClass.toDES(Capital) mustBe "Capital share"
      ShareClass.toDES(Growth) mustBe "Other"
      ShareClass.toDES(Other) mustBe "Other"
    }

    "return the correct value for fromDES " in {
      ShareClass.fromDES("Ordinary shares") mustBe Ordinary
      ShareClass.fromDES("Non-voting shares") mustBe NonVoting
      ShareClass.fromDES("Redeemable shares") mustBe Redeemable
      ShareClass.fromDES("Preference shares") mustBe Preference
      ShareClass.fromDES("Deferred ordinary shares") mustBe Deferred
      ShareClass.fromDES("Management shares") mustBe Management
      ShareClass.fromDES("Other classes of shares") mustBe OtherClasses
      ShareClass.fromDES("Voting shares") mustBe Voting
      ShareClass.fromDES("Dividend shares") mustBe Dividend
      ShareClass.fromDES("Capital share") mustBe Capital
      ShareClass.fromDES("Other") mustBe Other
    }

    "return the correct value when converting toDES then fromDES " in {
      ShareClass.fromDES(ShareClass.toDES(Growth)) mustBe Other
      ShareClass.fromDES(ShareClass.toDES(Other)) mustBe Other
    }
  }
}
