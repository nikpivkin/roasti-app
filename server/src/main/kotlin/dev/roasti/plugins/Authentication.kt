package dev.roasti.plugins

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dev.roasti.FIREBASE_AUTH
import dev.roasti.FirebasePrincipal
import dev.roasti.features.users.model.UserId
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.bearer
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun Application.configureAuthentication() {
  val firebaseAuth = FirebaseAuth.getInstance()
  install(Authentication) {
    bearer(FIREBASE_AUTH) {
      authenticate { credential ->
        try {
          val decoded = firebaseAuth.verifyIdToken(credential.token)
          val id = decoded.claims["id"] as? String ?: return@authenticate null
          FirebasePrincipal(UserId(Uuid.parse(id)))
        } catch (_: FirebaseAuthException) {
          null
        }
      }
    }
  }
}
