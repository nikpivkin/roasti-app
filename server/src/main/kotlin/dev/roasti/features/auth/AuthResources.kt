package dev.roasti.features.auth

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

@Serializable
@Resource("/auth")
class Auth {
  @Serializable @Resource("register") class Register(val parent: Auth = Auth())

  @Serializable @Resource("login") class Login(val parent: Auth = Auth())

  @Serializable @Resource("refresh") class Refresh(val parent: Auth = Auth())

  @Serializable @Resource("logout") class Logout(val parent: Auth = Auth())
}
