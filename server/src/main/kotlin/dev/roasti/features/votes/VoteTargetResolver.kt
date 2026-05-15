package dev.roasti.features.votes

import arrow.core.Either
import dev.roasti.features.posts.PostId
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
sealed class VoteTarget {
  abstract val type: VoteTargetType

  data class Post(val id: PostId) : VoteTarget() {
    override val type = VoteTargetType.POST
  }
}

sealed interface VoteTargetError {
  data object NotFound : VoteTargetError
}

interface VoteTargetResolver {
  suspend fun resolve(target: VoteTarget): Either<VoteTargetError, Unit>
}
