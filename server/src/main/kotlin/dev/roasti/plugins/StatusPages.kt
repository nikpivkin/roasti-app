package dev.roasti.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
  install(StatusPages) {
    exception<io.ktor.server.plugins.BadRequestException> { call, _ ->
      call.respond(HttpStatusCode.BadRequest)
    }
    exception<IllegalArgumentException> { call, cause ->
      call.respond(HttpStatusCode.UnprocessableEntity, cause.message ?: "validation error")
    }
    exception<IllegalStateException> { call, cause ->
      call.respond(HttpStatusCode.Conflict, cause.message ?: "conflict")
    }
    exception<Throwable> { call, cause ->
      call.application.log.error("Unhandled exception", cause)
      call.respond(HttpStatusCode.InternalServerError)
    }
  }
}
