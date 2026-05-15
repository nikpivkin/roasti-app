package dev.roasti.features.recipes

import dev.roasti.feature.recipe.data.remote.model.BrewMethodDto
import dev.roasti.feature.recipe.data.remote.model.DifficultyDto
import dev.roasti.feature.recipe.data.remote.model.RoastLevelDto
import dev.roasti.features.users.model.UserId
import io.ktor.resources.Resource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Resource("/recipes")
class Recipes(
    val page: Int = 1,
    val limit: Int = 20,
    @SerialName("author_id") val authorId: UserId? = null,
    @SerialName("brew_method") val brewMethod: BrewMethodDto? = null,
    val difficulty: DifficultyDto? = null,
    @SerialName("roast_level") val roastLevel: RoastLevelDto? = null,
) {
  @Serializable
  @Resource("{id}")
  data class ById(val parent: Recipes = Recipes(), val id: RecipeId) {
    @Serializable
    @Resource("comments")
    data class Comments(val parent: ById, val page: Int = 1, val limit: Int = 20)

    @Serializable @Resource("like") class Like(val parent: ById)

    @Serializable @Resource("clone") class Clone(val parent: ById)
  }
}
