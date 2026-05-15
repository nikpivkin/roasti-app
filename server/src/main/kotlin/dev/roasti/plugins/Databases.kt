package dev.roasti.plugins

import dev.roasti.config.DatabaseConfig
import dev.roasti.features.auth.RevokedTokenTable
import dev.roasti.features.comments.CommentTable
import dev.roasti.features.likes.LikeTable
import dev.roasti.features.posts.PostTable
import dev.roasti.features.recipes.BrewStepTable
import dev.roasti.features.recipes.RecipeTable
import dev.roasti.features.uploads.UploadTable
import dev.roasti.features.users.UserTable
import dev.roasti.features.votes.VoteTable
import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDatabases(config: DatabaseConfig) {
  Database.connect(url = config.url, driver = config.driver)
  transaction {
    SchemaUtils.create(
        UserTable,
        RevokedTokenTable,
        VoteTable,
        LikeTable,
        CommentTable,
        PostTable,
        RecipeTable,
        BrewStepTable,
        UploadTable,
    )
  }
}
