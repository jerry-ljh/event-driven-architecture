package com.example.usercore.service

import com.example.usercore.domain.User
import com.example.usercore.dto.Action
import com.example.usercore.dto.UserEventRequest
import com.example.usercore.repository.UserJpaRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserJpaRepository,
    private val couponApiService: CouponApiService,
    private val notificationApiService: NotificationApiService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(this::class.simpleName)

    @Transactional
    fun createUser(userId: String, userName: String): User {
        val user = userRepository.findByUserId(userId)
        if (user != null) return user
        val newUser = User(userId = userId, name = userName)
        userRepository.save(newUser)
        log.info("user 생성 $userId")
        couponApiService.createNewUserCoupon(newUser.id)
        notificationApiService.sendNewUserNotification(newUser.id)
        return newUser
    }

    @Transactional
    fun createUserWithEvent(userId: String, userName: String): User {
        val user = userRepository.findByUserId(userId)
        if (user != null) return user
        val newUser = User(userId = userId, name = userName)
        userRepository.save(newUser)
        log.info("user 생성 $userId")
        applicationEventPublisher.publishEvent(UserEventRequest(id = newUser.id, action = Action.CREATE))
        return newUser
    }
}
