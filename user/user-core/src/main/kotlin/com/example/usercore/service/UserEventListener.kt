package com.example.usercore.service

import com.example.usercore.dto.UserEventPayload
import com.example.usercore.dto.UserEventRequest
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class UserEventListener(
    private val userEventService: UserEventService
) {

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun userEventRequestListener(userEventRequest: UserEventRequest) {
        userEventService.saveUserEvent(userEventRequest)
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun internalUserEventListener(userEventPayload: UserEventPayload) {
        userEventService.publishUserEvent(userEventPayload)
    }
}
