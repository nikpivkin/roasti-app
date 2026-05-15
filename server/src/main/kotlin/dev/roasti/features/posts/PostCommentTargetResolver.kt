package dev.roasti.features.posts

import arrow.core.Either
import arrow.core.raise.either
import dev.roasti.features.comments.CommentTarget
import dev.roasti.features.comments.CommentTargetResolver
import dev.roasti.features.comments.ResolvedTarget
import dev.roasti.features.comments.TargetError
import dev.roasti.features.users.model.UserId

class PostCommentTargetResolver(
    private val repo: PostRepository,
) : CommentTargetResolver {

  override suspend fun resolve(
      target: CommentTarget,
      userId: UserId,
  ): Either<TargetError, ResolvedTarget> = either {
    val post = target as CommentTarget.Post
    repo.findById(post.id) ?: raise(TargetError.NotFound)
    ResolvedTarget(target.type, post.id.value)
  }
}
