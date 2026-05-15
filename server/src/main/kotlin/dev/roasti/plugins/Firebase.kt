package dev.roasti.plugins

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dev.roasti.config.FirebaseConfig
import io.ktor.server.application.Application
import java.io.ByteArrayInputStream
import java.util.Base64

fun Application.initFirebase(config: FirebaseConfig) {
  if (FirebaseApp.getApps().isNotEmpty()) return

  val isEmulator = System.getenv("FIREBASE_AUTH_EMULATOR_HOST") != null

  val credentials =
      when {
        !config.credentialsBase64.isNullOrBlank() ->
            GoogleCredentials.fromStream(
                ByteArrayInputStream(Base64.getDecoder().decode(config.credentialsBase64))
            )
        isEmulator -> GoogleCredentials.newBuilder().build()
        else -> GoogleCredentials.getApplicationDefault()
      }

  FirebaseApp.initializeApp(
      FirebaseOptions.builder()
          .setCredentials(credentials)
          .apply { if (config.projectId != null) setProjectId(config.projectId) }
          .build()
  )
}
