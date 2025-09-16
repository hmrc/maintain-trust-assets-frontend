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

import java.time.{LocalDate, LocalDateTime}
import play.api.Logging
import play.api.libs.functional.syntax._
import play.api.libs.json._
import queries.{Gettable, Settable}
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
                              internalId: String,
                              identifier: String,
                              sessionId: String,
                              newId: String,
                              whenTrustSetup: LocalDate,
                              data: JsObject = Json.obj(),
                              updatedAt: LocalDateTime = LocalDateTime.now,
                              is5mldEnabled: Boolean = false,
                              isTaxable: Boolean = true,
                              isUnderlyingData5mld: Boolean = false,
                              isMigratingToTaxable: Boolean = false
                            ) extends Logging {

  def cleanup: Try[UserAnswers] =
    cleanupBase(preserveMoney = false, preserveOther = false, preservePartnership = false,
      preservePropertyOrLand = false, preserveShares = false, preserveNonEea = false, preserveBusiness = false)

  def cleanupPreservingBusiness: Try[UserAnswers] =
    cleanupBase(preserveMoney = false, preserveOther = false, preservePartnership = false,
      preservePropertyOrLand = false, preserveShares = false, preserveNonEea = false, preserveBusiness = true)

  def cleanupPreservingMoney: Try[UserAnswers] =
    cleanupBase(preserveMoney = true, preserveOther = false, preservePartnership = false,
      preservePropertyOrLand = false, preserveShares = false, preserveNonEea = false, preserveBusiness = false)

  def cleanupPreservingOther: Try[UserAnswers] =
    cleanupBase(preserveMoney = false, preserveOther = true, preservePartnership = false,
      preservePropertyOrLand = false, preserveShares = false, preserveNonEea = false, preserveBusiness = false)

  def cleanupPreservingPartnership: Try[UserAnswers] =
    cleanupBase(preserveMoney = false, preserveOther = false, preservePartnership = true,
      preservePropertyOrLand = false, preserveShares = false, preserveNonEea = false, preserveBusiness = false)

  def cleanupPreservingPropertyOrLand: Try[UserAnswers] =
    cleanupBase(preserveMoney = false, preserveOther = false, preservePartnership = false,
      preservePropertyOrLand = true, preserveShares = false, preserveNonEea = false, preserveBusiness = false)

  def cleanupPreservingShares: Try[UserAnswers] =
    cleanupBase(preserveMoney = false, preserveOther = false, preservePartnership = false,
      preservePropertyOrLand = false, preserveShares = true, preserveNonEea = false, preserveBusiness = false)

  def cleanupPreservingNonEea: Try[UserAnswers] =
    cleanupBase(preserveMoney = false, preserveOther = false, preservePartnership = false,
      preservePropertyOrLand = false, preserveShares = false, preserveNonEea = true, preserveBusiness = false)

  private def cleanupBase(
                           preserveBusiness: Boolean,
                           preserveMoney: Boolean,
                           preserveOther: Boolean,
                           preservePartnership: Boolean,
                           preservePropertyOrLand: Boolean,
                           preserveShares: Boolean,
                           preserveNonEea: Boolean
                         ): Try[UserAnswers] = {
    val afterMoney   = if (preserveMoney) Success(this) else this.deleteAtPath(pages.asset.money.basePath)
    val afterOther   = afterMoney.flatMap(u => if (preserveOther) Success(u) else u.deleteAtPath(pages.asset.other.basePath))
    val afterPartner = afterOther.flatMap(u => if (preservePartnership) Success(u) else u.deleteAtPath(pages.asset.partnership.basePath))
    val afterPoL     = afterPartner.flatMap(u => if (preservePropertyOrLand) Success(u) else u.deleteAtPath(pages.asset.property_or_land.basePath))
    val afterShares  = afterPoL.flatMap(u => if (preserveShares) Success(u) else u.deleteAtPath(pages.asset.shares.basePath))
    val afterNonEea  = afterShares.flatMap(u => if (preserveNonEea) Success(u) else u.deleteAtPath(pages.asset.noneeabusiness.basePath))
    val afterBiz     = afterNonEea.flatMap(u => if (preserveBusiness) Success(u) else u.deleteAtPath(pages.asset.business.basePath))
    val afterKind    = afterBiz.flatMap(_.deleteAtPath(pages.asset.WhatKindOfAssetPage(0).path))
    afterKind.flatMap(_.remove(pages.asset.AddNowPage))
  }

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.at(page.path).reads(data) match {
      case JsSuccess(value, _) => Some(value)
      case JsError(errors)     => logger.info(s"Tried to read path ${page.path} errors: $errors"); None
    }

  def set[A](page: Settable[A], value: Option[A])(implicit writes: Writes[A]): Try[UserAnswers] = {
    value match {
      case Some(v) => setValue(page, v)
      case None =>
        val updatedAnswers = this
        page.cleanup(value, updatedAnswers)
    }
  }

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = setValue(page, value)

  private def setValue[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {
    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](query: Settable[A]): Try[UserAnswers] = {
    val updatedData = data.removeObject(query.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy (data = d)
        query.cleanup(None, updatedAnswers)
    }
    updatedData.flatMap { d => val updatedAnswers = copy(data = d); query.cleanup(None, updatedAnswers) }
  }

  def deleteAtPath(path: JsPath): Try[UserAnswers] = {
    data.removeObject(path).map(obj => copy(data = obj)).fold(
      _ => Success(this),
      result => Success(result)
    )
  }
}

object UserAnswers {
  implicit lazy val reads: Reads[UserAnswers] = (
    (__ \ "internalId").read[String] and
      ((__ \ "utr").read[String] or (__ \ "identifier").read[String]) and
      (__ \ "sessionId").read[String] and
      (__ \ "newId").read[String] and
      (__ \ "whenTrustSetup").read[LocalDate] and
      (__ \ "data").read[JsObject] and
      (__ \ "updatedAt").read(MongoDateTimeFormats.localDateTimeRead) and
      (__ \ "is5mldEnabled").readWithDefault[Boolean](false) and
      (__ \ "isTaxable").readWithDefault[Boolean](true) and
      (__ \ "isUnderlyingData5mld").readWithDefault[Boolean](false) and
      (__ \ "migratingFromNonTaxableToTaxable").readWithDefault[Boolean](false)
    )(UserAnswers.apply _)

  implicit lazy val writes: OWrites[UserAnswers] = (
    (__ \ "internalId").write[String] and
      (__ \ "utr").write[String] and
      (__ \ "sessionId").write[String] and
      (__ \ "newId").write[String] and
      (__ \ "whenTrustSetup").write[LocalDate] and
      (__ \ "data").write[JsObject] and
      (__ \ "updatedAt").write(MongoDateTimeFormats.localDateTimeWrite) and
      (__ \ "is5mldEnabled").write[Boolean] and
      (__ \ "isTaxable").write[Boolean] and
      (__ \ "isUnderlyingData5mld").write[Boolean] and
      (__ \ "migratingFromNonTaxableToTaxable").write[Boolean]
    )(unlift(UserAnswers.unapply))
}
