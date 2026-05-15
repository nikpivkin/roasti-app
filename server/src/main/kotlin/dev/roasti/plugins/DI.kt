package dev.roasti.plugins

import com.google.firebase.auth.FirebaseAuth
import dev.roasti.features.auth.FirebaseSigner
import dev.roasti.features.auth.FirebaseSignerImpl
import dev.roasti.features.auth.RevokedTokenRepository
import dev.roasti.features.auth.RevokedTokenRepositoryImpl
import dev.roasti.features.auth.usecase.Login
import dev.roasti.features.auth.usecase.Logout
import dev.roasti.features.auth.usecase.RefreshToken
import dev.roasti.features.auth.usecase.Register
import dev.roasti.features.comments.CommentRepository
import dev.roasti.features.comments.CommentRepositoryImpl
import dev.roasti.features.comments.CommentService
import dev.roasti.features.comments.CommentServiceImpl
import dev.roasti.features.comments.CommentTargetType
import dev.roasti.features.likes.LikeRepository
import dev.roasti.features.likes.LikeRepositoryImpl
import dev.roasti.features.likes.LikeService
import dev.roasti.features.likes.LikeServiceImpl
import dev.roasti.features.likes.LikeTargetType
import dev.roasti.features.posts.PostCommentTargetResolver
import dev.roasti.features.posts.PostRepository
import dev.roasti.features.posts.PostRepositoryImpl
import dev.roasti.features.posts.PostService
import dev.roasti.features.posts.PostServiceImpl
import dev.roasti.features.posts.PostVoteTargetResolver
import dev.roasti.features.recipes.RecipeCommentTargetResolver
import dev.roasti.features.recipes.RecipeLikeTargetResolver
import dev.roasti.features.recipes.RecipeRepository
import dev.roasti.features.recipes.RecipeRepositoryImpl
import dev.roasti.features.recipes.RecipeService
import dev.roasti.features.recipes.RecipeServiceImpl
import dev.roasti.features.uploads.FileStorage
import dev.roasti.features.uploads.LocalFileStorage
import dev.roasti.features.uploads.UploadRepository
import dev.roasti.features.uploads.UploadRepositoryImpl
import dev.roasti.features.uploads.UploadService
import dev.roasti.features.uploads.UploadServiceImpl
import dev.roasti.features.users.UserRepository
import dev.roasti.features.users.UserRepositoryImpl
import dev.roasti.features.users.usecase.CheckUsernameAvailability
import dev.roasti.features.users.usecase.GetCurrentUser
import dev.roasti.features.users.usecase.GetUserProfile
import dev.roasti.features.users.usecase.UpdateProfile
import dev.roasti.features.votes.VoteRepository
import dev.roasti.features.votes.VoteRepositoryImpl
import dev.roasti.features.votes.VoteService
import dev.roasti.features.votes.VoteServiceImpl
import dev.roasti.features.votes.VoteTargetType
import io.ktor.server.application.Application
import io.ktor.server.application.install
import java.io.File
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun Application.configureDI() {
  val uploadsDir =
      File(environment.config.propertyOrNull("uploads.dir")?.getString() ?: "./uploads")
  val firebaseApiKey = environment.config.property("firebase.apiKey").getString()
  val identityBaseUrl =
      environment.config.propertyOrNull("firebase.identityBaseUrl")?.getString()
          ?: "https://identitytoolkit.googleapis.com/v1/accounts"
  val tokenBaseUrl =
      environment.config.propertyOrNull("firebase.tokenBaseUrl")?.getString()
          ?: "https://securetoken.googleapis.com/v1/token"

  install(Koin) {
    modules(
        module {
          single<UserRepository> { UserRepositoryImpl() }
          single { GetCurrentUser(get()) }
          single { GetUserProfile(get()) }
          single { UpdateProfile(get(), get()) }
          single { CheckUsernameAvailability(get()) }
          single<FirebaseSigner> {
            FirebaseSignerImpl(firebaseApiKey, identityBaseUrl, tokenBaseUrl)
          }
          single<RevokedTokenRepository> { RevokedTokenRepositoryImpl() }
          single { FirebaseAuth.getInstance() }
          single<CommentRepository> { CommentRepositoryImpl() }
          single<PostRepository> { PostRepositoryImpl() }
          single<LikeRepository> { LikeRepositoryImpl() }
          single<LikeService> {
            LikeServiceImpl(
                repo = get(),
                resolvers = mapOf(LikeTargetType.RECIPE to RecipeLikeTargetResolver(get())),
            )
          }
          single<VoteRepository> { VoteRepositoryImpl() }
          single<VoteService> {
            VoteServiceImpl(
                repo = get(),
                resolvers = mapOf(VoteTargetType.POST to PostVoteTargetResolver(get())),
            )
          }
          single<RecipeRepository> { RecipeRepositoryImpl() }
          single<RecipeService> { RecipeServiceImpl(get(), get(), get()) }
          single<CommentService> {
            CommentServiceImpl(
                repo = get(),
                resolvers =
                    mapOf(
                        CommentTargetType.POST to PostCommentTargetResolver(get()),
                        CommentTargetType.RECIPE to RecipeCommentTargetResolver(get()),
                    ),
            )
          }
          single<PostService> { PostServiceImpl(get(), get(), get(), get(), get()) }
          single<FileStorage> { LocalFileStorage(uploadsDir) }
          single<UploadRepository> { UploadRepositoryImpl() }
          single<UploadService> { UploadServiceImpl(get(), get()) }
          single { Register(get(), get(), get()) }
          single { Login(get(), get()) }
          single { RefreshToken(get(), get()) }
          single { Logout(get()) }
        }
    )
  }
}
