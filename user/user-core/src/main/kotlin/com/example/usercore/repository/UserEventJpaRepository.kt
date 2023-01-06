package com.example.usercore.repository

import com.example.usercore.domain.UserEvent
import org.springframework.data.jpa.repository.JpaRepository

interface UserEventJpaRepository : JpaRepository<UserEvent, Long> {
    fun findByEvent(event: Map<String, Any>): UserEvent?
}
