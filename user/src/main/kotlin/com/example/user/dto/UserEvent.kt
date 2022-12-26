package com.example.user.dto

data class UserEvent(
    val id: Long,
    val action: Action
)

enum class Action {
    CREATE, UPDATE
}
