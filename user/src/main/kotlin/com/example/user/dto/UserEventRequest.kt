package com.example.user.dto

import com.example.user.domain.UserEvent
import com.example.user.util.toMap

data class UserEventRequest(
    val id: Long,
    val action: Action
) {
    fun toEventEntity(): UserEvent {
        return UserEvent(event = this.toMap())
    }
}

enum class Action {
    CREATE, UPDATE
}
