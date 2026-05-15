package dev.roasti.features.likes

import arrow.core.Either
import dev.roasti.features.recipes.RecipeId
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
sealed class LikeTarget {
  abstract val type: LikeTargetType

  data class Recipe(val id: RecipeId) : LikeTarget() {
    override val type = LikeTargetType.RECIPE
  }
}

sealed interface LikeTargetError {
  data object NotFound : LikeTargetError
}

interface LikeTargetResolver {
  suspend fun resolve(target: LikeTarget): Either<LikeTargetError, Unit>
}
