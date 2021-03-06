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

package repositories

import com.google.inject.ImplementedBy
import models.{MongoDateTimeFormats, UtrSession}
import play.api.Configuration
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.WriteConcern
import reactivemongo.api.indexes.IndexType
import reactivemongo.play.json.collection.JSONCollection

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ActiveSessionRepositoryImpl @Inject()(override val mongo: ReactiveMongoApi,
                                            override val config: Configuration
                                           )(override implicit val ec: ExecutionContext)
  extends ActiveSessionRepository
    with IndexManager {

  override val collectionName: String = "session"

  private val cacheTtl = config.get[Int]("mongodb.session.ttlSeconds")

  private def collection: Future[JSONCollection] =
    for {
      _ <- ensureIndexes
      res <- mongo.database.map(_.collection[JSONCollection](collectionName))
    } yield res

  private val lastUpdatedIndex = MongoIndex(
    key = Seq("updatedAt" -> IndexType.Ascending),
    name = "session-updated-at-index",
    expireAfterSeconds = Some(cacheTtl)
  )

  private val utrIndex = MongoIndex(
    key = Seq("utr" -> IndexType.Ascending),
    name = "utr-index"
  )

  private lazy val ensureIndexes = for {
      collection              <- mongo.database.map(_.collection[JSONCollection](collectionName))
      createdLastUpdatedIndex <- collection.indexesManager.ensure(lastUpdatedIndex)
      createdIdIndex          <- collection.indexesManager.ensure(utrIndex)
    } yield createdLastUpdatedIndex && createdIdIndex

  override def get(internalId: String): Future[Option[UtrSession]] = {

    logger.debug(s"ActiveSessionRepository getting active utr for $internalId")

    val selector = Json.obj("internalId" -> internalId)

    val modifier = Json.obj(
      "$set" -> Json.obj(
        "updatedAt" -> MongoDateTimeFormats.localDateTimeWrite.writes(LocalDateTime.now)
      )
    )

    for {
      col <- collection
      r <- col.findAndUpdate(
        selector = selector,
        update = modifier,
        fetchNewObject = true,
        upsert = false,
        sort = None,
        fields = None,
        bypassDocumentValidation = false,
        writeConcern = WriteConcern.Default,
        maxTime = None,
        collation = None,
        arrayFilters = Nil)
    } yield r.result[UtrSession]
  }

  override def set(session: UtrSession): Future[Boolean] = {

    val selector = Json.obj("internalId" -> session.internalId)

    val modifier = Json.obj(
      "$set" -> (session.copy(updatedAt = LocalDateTime.now))
    )

    for {
      col <- collection
      r <- col.update(ordered = false).one(selector, modifier, upsert = true, multi = false)
    } yield r.ok
  }
}

@ImplementedBy(classOf[ActiveSessionRepositoryImpl])
trait ActiveSessionRepository {

  def get(internalId: String): Future[Option[UtrSession]]

  def set(session: UtrSession): Future[Boolean]
}
