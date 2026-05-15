package dev.roasti.plugins

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.application.Application
import java.io.ByteArrayInputStream
import java.util.Base64

fun Application.initFirebase() {
  if (FirebaseApp.getApps().isNotEmpty()) return

  val credsBase64 = environment.config.propertyOrNull("firebase.credentialsBase64")?.getString()
  val isEmulator = System.getenv("FIREBASE_AUTH_EMULATOR_HOST") != null

  val credentials =
      when {
        !credsBase64.isNullOrBlank() ->
            GoogleCredentials.fromStream(
                ByteArrayInputStream(Base64.getDecoder().decode(credsBase64))
            )
        isEmulator -> GoogleCredentials.newBuilder().build()
        else -> GoogleCredentials.getApplicationDefault()
      }

  val projectId = environment.config.propertyOrNull("firebase.projectId")?.getString()

  FirebaseApp.initializeApp(
      FirebaseOptions.builder()
          .setCredentials(credentials)
          .apply { if (projectId != null) setProjectId(projectId) }
          .build()
  )
}
