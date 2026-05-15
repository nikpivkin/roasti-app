package dev.roasti.features.votes

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dev.roasti.features.users.model.UserId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface VoteService {
  suspend fun toggle(
      userId: UserId,
      target: VoteTarget,
      direction: VoteDirection,
  ): Either<VoteTargetError, VoteInfo>

  suspend fun getInfo(userId: UserId?, targetId: Uuid, targetType: VoteTargetType): VoteInfo

  suspend fun getInfoBatch(
      userId: UserId?,
      targetIds: List<Uuid>,
      targetType: VoteTargetType,
  ): Map<Uuid, VoteInfo>
}

@OptIn(ExperimentalUuidApi::class)
class VoteServiceImpl(
    private val repo: VoteRepository,
    private val resolvers: Map<VoteTargetType, VoteTargetResolver>,
) : VoteService {
  override suspend fun toggle(
      userId: UserId,
      target: VoteTarget,
      direction: VoteDirection,
  ): Either<VoteTargetError, VoteInfo> = either {
    val resolver = ensureNotNull(resolvers[target.type]) { VoteTargetError.NotFound }
    resolver.resolve(target).bind()

    when (direction) {
      VoteDirection.UP,
      VoteDirection.DOWN -> repo.upsert(userId, target.targetId, target.type, direction)

      VoteDirection.NONE -> repo.delete(userId, target.targetId, target.type)
    }
    getInfo(userId, target.targetId, target.type)
  }

  override suspend fun getInfo(
      userId: UserId?,
      targetId: Uuid,
      targetType: VoteTargetType,
  ): VoteInfo = getInfoBatch(userId, listOf(targetId), targetType)[targetId] ?: VoteInfo.EMPTY

  override suspend fun getInfoBatch(
      userId: UserId?,
      targetIds: List<Uuid>,
      targetType: VoteTargetType,
  ): Map<Uuid, VoteInfo> {
    if (targetIds.isEmpty()) return emptyMap()
    val ratings = repo.fetchRatings(targetIds, targetType)
    val userVotes =
        if (userId == null) emptyMap() else repo.fetchUserVotes(userId, targetIds, targetType)

    return targetIds.associateWith { targetId ->
      VoteInfo(
          rating = ratings[targetId] ?: 0,
          userVote = userVotes[targetId] ?: VoteDirection.NONE,
      )
    }
  }
}

@OptIn(ExperimentalUuidApi::class)
private val VoteTarget.targetId: Uuid
  get() =
      when (this) {
        is VoteTarget.Post -> id.value
      }
