package dev.roasti.config

import io.ktor.server.application.Application
import java.io.File

data class AppConfig(
    val database: DatabaseConfig,
    val firebase: FirebaseConfig,
    val uploads: UploadsConfig,
)

data class DatabaseConfig(
    val url: String,
    val driver: String,
)

data class FirebaseConfig(
    val apiKey: String,
    val credentialsBase64: String?,
    val projectId: String?,
    val identityBaseUrl: String,
    val tokenBaseUrl: String,
)

data class UploadsConfig(
    val dir: File,
)

fun Application.loadConfig(): AppConfig {
  val env = environment.config
  return AppConfig(
      database =
          DatabaseConfig(
              url = env.property("database.url").getString(),
              driver = env.property("database.driver").getString(),
          ),
      firebase =
          FirebaseConfig(
              apiKey = env.property("firebase.apiKey").getString(),
              credentialsBase64 = env.propertyOrNull("firebase.credentialsBase64")?.getString(),
              projectId = env.propertyOrNull("firebase.projectId")?.getString(),
              identityBaseUrl =
                  env.propertyOrNull("firebase.identityBaseUrl")?.getString()
                      ?: "https://identitytoolkit.googleapis.com/v1/accounts",
              tokenBaseUrl =
                  env.propertyOrNull("firebase.tokenBaseUrl")?.getString()
                      ?: "https://securetoken.googleapis.com/v1/token",
          ),
      uploads =
          UploadsConfig(
              dir = File(env.propertyOrNull("uploads.dir")?.getString() ?: "./uploads"),
          ),
  )
}
