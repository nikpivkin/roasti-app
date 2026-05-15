package dev.roasti.features.recipes

import arrow.core.Either
import arrow.core.raise.either
import dev.roasti.features.comments.CommentTarget
import dev.roasti.features.comments.CommentTargetResolver
import dev.roasti.features.comments.ResolvedTarget
import dev.roasti.features.comments.TargetError
import dev.roasti.features.users.model.UserId

class RecipeCommentTargetResolver(
    private val repo: RecipeRepository,
) : CommentTargetResolver {

  override suspend fun resolve(
      target: CommentTarget,
      userId: UserId?,
  ): Either<TargetError, ResolvedTarget> = either {
    val recipe = target as CommentTarget.Recipe
    val row = repo.findById(recipe.id) ?: raise(TargetError.NotFound)
    if (!row.public && row.author.id != userId) raise(TargetError.NotVisible)
    ResolvedTarget(target.type, recipe.id.value)
  }
}
