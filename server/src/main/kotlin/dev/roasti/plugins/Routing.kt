package dev.roasti.plugins

import dev.roasti.features.auth.authRoutes
import dev.roasti.features.comments.commentRoutes
import dev.roasti.features.posts.postRoutes
import dev.roasti.features.recipes.recipeRoutes
import dev.roasti.features.uploads.uploadRoutes
import dev.roasti.features.users.userRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
  routing {
    get("/health") { call.respond(HttpStatusCode.OK, "OK") }
    route("/api/v1") {
      authRoutes()
      userRoutes()
      postRoutes()
      recipeRoutes()
      commentRoutes()
      uploadRoutes()
    }
  }
}
