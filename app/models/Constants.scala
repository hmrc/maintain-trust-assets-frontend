/*
 * Copyright 2021 HM Revenue & Customs
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

object Constants {

  final val MAX_LIMIT_FOR_MOST_ASSET_TYPES = 10
  final val MAX_MONEY_ASSETS = 1
  final val MAX_PROPERTY_OR_LAND_ASSETS = MAX_LIMIT_FOR_MOST_ASSET_TYPES
  final val MAX_SHARES_ASSETS = MAX_LIMIT_FOR_MOST_ASSET_TYPES
  final val MAX_BUSINESS_ASSETS = MAX_LIMIT_FOR_MOST_ASSET_TYPES
  final val MAX_NON_EEA_BUSINESS_ASSETS = 25
  final val MAX_PARTNERSHIP_ASSETS = MAX_LIMIT_FOR_MOST_ASSET_TYPES
  final val MAX_OTHER_ASSETS = MAX_LIMIT_FOR_MOST_ASSET_TYPES

  final val MAX_ALL_ASSETS = 50
}
