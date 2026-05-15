package dev.roasti.features.votes

import dev.roasti.features.users.model.UserId
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.case
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.intLiteral
import org.jetbrains.exposed.v1.core.sum
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

enum class VoteDirection(val score: Int) {
  UP(1),
  DOWN(-1),
  NONE(0),
}

enum class VoteTargetType {
  POST
}

data class VoteInfo(val rating: Int, val userVote: VoteDirection) {
  companion object {
    val EMPTY = VoteInfo(0, VoteDirection.NONE)
  }
}

@OptIn(ExperimentalUuidApi::class)
interface VoteRepository {
  suspend fun upsert(
      userId: UserId,
      targetId: Uuid,
      targetType: VoteTargetType,
      direction: VoteDirection,
  )

  suspend fun delete(userId: UserId, targetId: Uuid, targetType: VoteTargetType)

  suspend fun fetchRatings(targetIds: List<Uuid>, targetType: VoteTargetType): Map<Uuid, Int>

  suspend fun fetchUserVotes(
      userId: UserId,
      targetIds: List<Uuid>,
      targetType: VoteTargetType,
  ): Map<Uuid, VoteDirection>
}

@OptIn(ExperimentalUuidApi::class)
class VoteRepositoryImpl : VoteRepository {

  override suspend fun upsert(
      userId: UserId,
      targetId: Uuid,
      targetType: VoteTargetType,
      direction: VoteDirection,
  ): Unit =
      withContext(Dispatchers.IO) {
        transaction {
          VoteTable.upsert {
            it[VoteTable.userId] = userId.value
            it[VoteTable.targetId] = targetId
            it[VoteTable.targetType] = targetType
            it[VoteTable.voteType] = direction
            it[VoteTable.createdAt] = Clock.System.now()
          }
        }
      }

  override suspend fun delete(userId: UserId, targetId: Uuid, targetType: VoteTargetType): Unit =
      withContext(Dispatchers.IO) {
        transaction {
          VoteTable.deleteWhere {
            (VoteTable.userId eq userId.value) and
                (VoteTable.targetId eq targetId) and
                (VoteTable.targetType eq targetType)
          }
        }
      }

  override suspend fun fetchRatings(
      targetIds: List<Uuid>,
      targetType: VoteTargetType,
  ): Map<Uuid, Int> =
      withContext(Dispatchers.IO) {
        transaction {
          val scoreExpr =
              case(VoteTable.voteType)
                  .When(VoteDirection.UP, intLiteral(1))
                  .When(VoteDirection.DOWN, intLiteral(-1))
                  .Else(intLiteral(0))

          val sumExpr = scoreExpr.sum()

          VoteTable.select(VoteTable.targetId, sumExpr)
              .where((VoteTable.targetId inList targetIds) and (VoteTable.targetType eq targetType))
              .groupBy(VoteTable.targetId)
              .associate { it[VoteTable.targetId] to (it[sumExpr] ?: 0) }
        }
      }

  override suspend fun fetchUserVotes(
      userId: UserId,
      targetIds: List<Uuid>,
      targetType: VoteTargetType,
  ): Map<Uuid, VoteDirection> =
      withContext(Dispatchers.IO) {
        transaction {
          VoteTable.select(VoteTable.targetId, VoteTable.voteType)
              .where {
                (VoteTable.targetId inList targetIds) and
                    (VoteTable.targetType eq targetType) and
                    (VoteTable.userId eq userId.value)
              }
              .associate { it[VoteTable.targetId] to it[VoteTable.voteType] }
        }
      }
}
