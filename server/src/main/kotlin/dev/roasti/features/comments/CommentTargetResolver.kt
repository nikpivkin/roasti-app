package dev.roasti.features.comments

import arrow.core.Either
import dev.roasti.features.users.model.UserId
import kotlin.uuid.Uuid

data class ResolvedTarget(val targetType: CommentTargetType, val targetId: Uuid)

sealed interface TargetError {
  data object NotFound : TargetError

  data object NotVisible : TargetError

  data object CommentsDisabled : TargetError
}

interface CommentTargetResolver {
  suspend fun resolve(target: CommentTarget, userId: UserId): Either<TargetError, ResolvedTarget>
}
