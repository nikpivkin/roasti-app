package dev.roasti.features.posts

import dev.roasti.features.users.model.UserId
import io.ktor.resources.Resource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Resource("/posts")
class Posts(
    val page: Int = 1,
    val limit: Int = 20,
    @SerialName("author_id") val authorId: UserId? = null,
) {
  @Serializable
  @Resource("{id}")
  data class ById(val parent: Posts = Posts(), val id: PostId) {
    @Serializable
    @Resource("comments")
    data class Comments(val parent: ById, val page: Int = 1, val limit: Int = 20)

    @Serializable @Resource("vote") class Vote(val parent: ById)
  }
}
