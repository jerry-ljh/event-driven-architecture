package com.example.usercore.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CouponApiService {

    private val log = LoggerFactory.getLogger(this::class.simpleName)

    fun createNewUserCoupon(userId: Long){
        log.info("신규 유저 쿠폰 발급 userId: $userId")
    }
}