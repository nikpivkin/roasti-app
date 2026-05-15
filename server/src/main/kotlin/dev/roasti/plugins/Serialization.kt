package dev.roasti.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources

fun Application.configureSerialization() {
  install(Resources)
  install(ContentNegotiation) { json(kotlinx.serialization.json.Json { explicitNulls = false }) }
}
