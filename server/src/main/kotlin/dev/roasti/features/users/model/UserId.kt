package dev.roasti.features.users.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class) @JvmInline @Serializable value class UserId(val value: Uuid)
