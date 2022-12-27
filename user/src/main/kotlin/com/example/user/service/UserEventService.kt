package com.example.user.service

import com.example.user.dto.UserEventRequest
import com.example.user.repository.UserEventJpaRepository
import com.example.user.util.toJson
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class UserEventService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val userEventJpaRepository: UserEventJpaRepository
) {
    private val log = LoggerFactory.getLogger(this::class.simpleName)
    private val topic = "user.v1"

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun saveUserEvent(userEventRequest: UserEventRequest) {
        userEventJpaRepository.save(userEventRequest.toEventEntity())
        log.info("save userEvent $userEventRequest")
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun createUserEvent(userEventRequest: UserEventRequest) {
        val value = userEventRequest.toJson()
        log.info("publish $topic, $value")
        kafkaTemplate.send(topic, value)
    }
}
