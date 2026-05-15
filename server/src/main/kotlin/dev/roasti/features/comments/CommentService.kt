package dev.roasti.features.comments

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import dev.roasti.common.domain.Page
import dev.roasti.features.posts.PostId
import dev.roasti.features.recipes.RecipeId
import dev.roasti.features.users.model.UserId
import kotlin.uuid.Uuid

const val TEXT_MAX_LENGTH = 1000

sealed interface CreateCommentError {
  data object TargetNotFound : CreateCommentError

  data object TargetNotVisible : CreateCommentError

  data object CommentsDisabled : CreateCommentError

  data object ParentNotFound : CreateCommentError

  data class InvalidInput(val message: String) : CreateCommentError
}

sealed interface UpdateCommentError {
  data object NotFound : UpdateCommentError

  data object Forbidden : UpdateCommentError

  data class InvalidInput(val message: String) : UpdateCommentError
}

sealed interface DeleteCommentError {
  data object NotFound : DeleteCommentError

  data object Forbidden : DeleteCommentError
}

enum class CommentTargetType {
  POST,
  RECIPE,
}

sealed class CommentTarget {
  abstract val type: CommentTargetType

  data class Post(val id: PostId) : CommentTarget() {
    override val type = CommentTargetType.POST
  }

  data class Recipe(val id: RecipeId) : CommentTarget() {
    override val type = CommentTargetType.RECIPE
  }
}

interface CommentService {
  suspend fun create(
      userId: UserId,
      target: CommentTarget,
      text: String,
      parentId: CommentId?,
  ): Either<CreateCommentError, Comment>

  suspend fun update(
      userId: UserId,
      commentId: CommentId,
      text: String,
  ): Either<UpdateCommentError, Comment>

  suspend fun delete(userId: UserId, commentId: CommentId): Either<DeleteCommentError, Unit>

  suspend fun list(target: CommentTarget, page: Int, limit: Int): Page<CommentThread>

  suspend fun countForTarget(targetId: Uuid, targetType: CommentTargetType): Int

  suspend fun countForTargetBatch(
      targetIds: List<Uuid>,
      targetType: CommentTargetType,
  ): Map<Uuid, Int>
}

class CommentServiceImpl(
    private val repo: CommentRepository,
    private val resolvers: Map<CommentTargetType, CommentTargetResolver>,
) : CommentService {

  override suspend fun create(
      userId: UserId,
      target: CommentTarget,
      text: String,
      parentId: CommentId?,
  ): Either<CreateCommentError, Comment> = either {
    val resolver = resolvers.getValue(target.type)
    resolver.resolve(target, userId).mapLeft { it.toCreateCommentError() }.bind()

    val normalized = text.trim()
    if (normalized.isBlank())
        raise(CreateCommentError.InvalidInput("comment text must not be empty"))
    if (normalized.length > TEXT_MAX_LENGTH)
        raise(
            CreateCommentError.InvalidInput(
                "comment text must be at most $TEXT_MAX_LENGTH characters"
            )
        )

    if (parentId != null && !repo.existsInTarget(parentId, target.targetId))
        raise(CreateCommentError.ParentNotFound)

    repo.create(
        CreateCommentInput(
            targetId = target.targetId,
            targetType = target.type,
            authorId = userId,
            text = normalized,
            parentId = parentId,
        )
    )
  }

  override suspend fun update(
      userId: UserId,
      commentId: CommentId,
      text: String,
  ): Either<UpdateCommentError, Comment> = either {
    val authorId = repo.getAuthorId(commentId) ?: raise(UpdateCommentError.NotFound)
    if (authorId != userId) raise(UpdateCommentError.Forbidden)
    val normalized = text.trim()
    ensure(normalized.isNotBlank()) {
      UpdateCommentError.InvalidInput("comment text must not be empty")
    }
    ensure(normalized.length <= TEXT_MAX_LENGTH) {
      UpdateCommentError.InvalidInput("comment text must be at most $TEXT_MAX_LENGTH characters")
    }
    // TODO: return comment from update and handle error
    repo.update(commentId, normalized)
    repo.findById(commentId)!!
  }

  override suspend fun delete(
      userId: UserId,
      commentId: CommentId,
  ): Either<DeleteCommentError, Unit> = either {
    val authorId = repo.getAuthorId(commentId) ?: raise(DeleteCommentError.NotFound)
    ensure(authorId != userId) { DeleteCommentError.Forbidden }
    repo.softDelete(commentId)
  }

  override suspend fun list(target: CommentTarget, page: Int, limit: Int): Page<CommentThread> {
    val (items, total) = repo.listForTarget(target.targetId, target.type, page, limit)
    return Page.of(items, page, total, limit)
  }

  override suspend fun countForTarget(targetId: Uuid, targetType: CommentTargetType): Int =
      repo.countForTargetBatch(listOf(targetId), targetType)[targetId] ?: 0

  override suspend fun countForTargetBatch(
      targetIds: List<Uuid>,
      targetType: CommentTargetType,
  ): Map<Uuid, Int> = repo.countForTargetBatch(targetIds, targetType)

  private fun TargetError.toCreateCommentError(): CreateCommentError =
      when (this) {
        TargetError.NotFound -> CreateCommentError.TargetNotFound
        TargetError.NotVisible -> CreateCommentError.TargetNotVisible
        TargetError.CommentsDisabled -> CreateCommentError.CommentsDisabled
      }
}

private val CommentTarget.targetId: Uuid
  get() =
      when (this) {
        is CommentTarget.Post -> id.value
        is CommentTarget.Recipe -> id.value
      }
