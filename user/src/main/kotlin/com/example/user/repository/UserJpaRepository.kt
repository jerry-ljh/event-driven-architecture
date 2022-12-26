package com.example.user.repository

import com.example.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository: JpaRepository<User, Long>{
    fun findByUserId(userId: String): User?
}
