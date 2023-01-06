package com.example.usercore.repository

import com.example.usercore.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository: JpaRepository<User, Long>{
    fun findByUserId(userId: String): User?
}
