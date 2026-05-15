package dev.roasti.features.recipes

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import dev.roasti.features.likes.LikeTarget
import dev.roasti.features.likes.LikeTargetError
import dev.roasti.features.likes.LikeTargetResolver
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RecipeLikeTargetResolver(private val repo: RecipeRepository) : LikeTargetResolver {
  override suspend fun resolve(target: LikeTarget): Either<LikeTargetError, Unit> = either {
    val recipe = target as LikeTarget.Recipe
    ensureNotNull(repo.findById(recipe.id)) { LikeTargetError.NotFound }
  }
}
