package com.example.user.repository

import com.example.user.domain.UserEvent
import org.springframework.data.jpa.repository.JpaRepository

interface UserEventJpaRepository : JpaRepository<UserEvent, Long> {
    fun findByEvent(event: Map<String, Any>): UserEvent?
}
