package dev.roasti.features.comments

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/comments")
class Comments {
  @Serializable
  @Resource("{id}")
  data class ById(val parent: Comments = Comments(), val id: CommentId)
}
