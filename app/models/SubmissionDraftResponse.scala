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

package models

import java.time.LocalDateTime

import play.api.libs.json.{JsValue, Json, OFormat}

// Primary front end draft data (e.g, trusts-frontend), including reference and in-progress.
case class SubmissionDraftData(data: JsValue, reference: Option[String], inProgress: Option[Boolean])

object SubmissionDraftData {
  implicit lazy val format: OFormat[SubmissionDraftData] = Json.format[SubmissionDraftData]
}

// Piece to be inserted into final registration data. JsNull means remove value.
case class SubmissionDraftRegistrationPiece(elementPath: String, data: JsValue)

object SubmissionDraftRegistrationPiece {
  implicit lazy val format: OFormat[SubmissionDraftRegistrationPiece] = Json.format[SubmissionDraftRegistrationPiece]
}

// In-progress or completed status for a particular section (front end).
case class SubmissionDraftStatus(section: String, status: Option[Status])

object SubmissionDraftStatus {
  implicit lazy val format: OFormat[SubmissionDraftStatus] = Json.format[SubmissionDraftStatus]
}

// Set of data sent by sub-frontend, with user answers, status and any registration pieces.
case class SubmissionDraftSetData(data: JsValue,
                                  status: Option[SubmissionDraftStatus],
                                  registrationPieces: List[SubmissionDraftRegistrationPiece])

object SubmissionDraftSetData {
  implicit lazy val format: OFormat[SubmissionDraftSetData] = Json.format[SubmissionDraftSetData]
}

// Responses
case class SubmissionDraftResponse(createdAt: LocalDateTime, data: JsValue, reference: Option[String])

object SubmissionDraftResponse {
  implicit lazy val format: OFormat[SubmissionDraftResponse] = Json.format[SubmissionDraftResponse]
}

case class SubmissionDraftId(draftId: String, createdAt: LocalDateTime, reference: Option[String])

object SubmissionDraftId {
  implicit lazy val format: OFormat[SubmissionDraftId] = Json.format[SubmissionDraftId]
}
