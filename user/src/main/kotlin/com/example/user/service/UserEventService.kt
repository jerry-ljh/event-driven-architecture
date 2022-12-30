package com.example.user.service

import com.example.user.dto.UserEventPayload
import com.example.user.dto.UserEventRequest
import com.example.user.repository.UserEventJpaRepository
import com.example.user.util.toJson
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class UserEventService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val userEventJpaRepository: UserEventJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(this::class.simpleName)
    private val topic = "user.v1"

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    fun saveUserEvent(userEventRequest: UserEventRequest) {
        val userEvent = userEventJpaRepository.save(userEventRequest.toEventEntity())
        log.info("save userEvent $userEventRequest")
        applicationEventPublisher.publishEvent(UserEventPayload(id = userEvent.id))
    }

    @Async
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun createUserEvent(userEventPayload: UserEventPayload) {
        val userEvent = userEventJpaRepository.findByIdOrNull(userEventPayload.id) ?: return
        val value = userEvent.event.toJson()
        kafkaTemplate.send(topic, value)
        log.info("published $topic, $value")
        userEvent.isPublished = true
    }
}
