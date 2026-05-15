package dev.roasti.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import java.util.UUID
import org.slf4j.event.Level

fun Application.configureLogging() {
  install(CallId) { generate { UUID.randomUUID().toString() } }
  install(CallLogging) {
    level = Level.INFO
    callIdMdc("requestId")
    format { call ->
      "${call.request.httpMethod.value} ${call.request.path()} -> ${call.response.status()}"
    }
  }
}
