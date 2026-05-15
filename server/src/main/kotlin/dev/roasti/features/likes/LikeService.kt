package dev.roasti.features.likes

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dev.roasti.features.users.model.UserId
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

interface LikeService {
  suspend fun toggle(userId: UserId, target: LikeTarget): Either<LikeTargetError, LikeInfo>

  suspend fun getInfo(userId: UserId?, targetId: Uuid, targetType: LikeTargetType): LikeInfo

  suspend fun getInfoBatch(
      userId: UserId?,
      targetIds: List<Uuid>,
      targetType: LikeTargetType,
  ): Map<Uuid, LikeInfo>
}

@OptIn(ExperimentalUuidApi::class)
class LikeServiceImpl(
    private val repo: LikeRepository,
    private val resolvers: Map<LikeTargetType, LikeTargetResolver>,
) : LikeService {

  override suspend fun toggle(
      userId: UserId,
      target: LikeTarget,
  ): Either<LikeTargetError, LikeInfo> = either {
    val resolver = ensureNotNull(resolvers[target.type]) { LikeTargetError.NotFound }
    resolver.resolve(target).bind()

    val targetId = target.targetId
    val exists = repo.exists(userId, targetId, target.type)
    if (exists) {
      repo.delete(userId, targetId, target.type)
    } else {
      repo.create(userId, targetId, target.type)
    }
    getInfo(userId, targetId, target.type)
  }

  override suspend fun getInfo(
      userId: UserId?,
      targetId: Uuid,
      targetType: LikeTargetType,
  ): LikeInfo = getInfoBatch(userId, listOf(targetId), targetType)[targetId] ?: LikeInfo.EMPTY

  override suspend fun getInfoBatch(
      userId: UserId?,
      targetIds: List<Uuid>,
      targetType: LikeTargetType,
  ): Map<Uuid, LikeInfo> {
    val rows = repo.getInfoBatch(userId, targetIds, targetType)
    val grouped = rows.groupBy { it.targetId }
    return targetIds.associateWith { id ->
      val group = grouped[id].orEmpty()
      LikeInfo(
          isLiked = userId != null && group.any { it.userId == userId },
          count = group.size,
      )
    }
  }
}

@OptIn(ExperimentalUuidApi::class)
private val LikeTarget.targetId: Uuid
  get() =
      when (this) {
        is LikeTarget.Recipe -> id.value
      }
