package com.example.usercore.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NotificationApiService {

    private val log = LoggerFactory.getLogger(this::class.simpleName)

    fun sendNewUserNotification(userId: Long){
        log.info("userId: $userId 생성 알림")
    }
}