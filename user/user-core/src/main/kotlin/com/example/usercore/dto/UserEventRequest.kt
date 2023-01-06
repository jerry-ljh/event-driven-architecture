package com.example.usercore.dto

import com.example.usercore.domain.UserEvent
import com.example.usercore.util.toMap

data class UserEventRequest(
    val id: Long,
    val action: Action,
) {
    fun toEventEntity(): UserEvent {
        return UserEvent(event = this.toMap())
    }
}

enum class Action {
    CREATE, UPDATE
}
