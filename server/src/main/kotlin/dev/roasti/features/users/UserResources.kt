package dev.roasti.features.users

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/users")
class Users {
  @Serializable @Resource("me") class Me(val parent: Users = Users())

  @Serializable
  @Resource("username-availability")
  class UsernameAvailability(val parent: Users = Users(), val username: String)

  @Serializable
  @Resource("{username}")
  data class ByUsername(val parent: Users = Users(), val username: String)
}
