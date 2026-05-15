package dev.roasti.features.posts

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dev.roasti.features.votes.VoteTarget
import dev.roasti.features.votes.VoteTargetError
import dev.roasti.features.votes.VoteTargetResolver
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class PostVoteTargetResolver(private val repo: PostRepository) : VoteTargetResolver {
  override suspend fun resolve(target: VoteTarget): Either<VoteTargetError, Unit> = either {
    val post = target as VoteTarget.Post
    ensureNotNull(repo.findById(post.id)) { VoteTargetError.NotFound }
  }
}
