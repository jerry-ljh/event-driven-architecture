package com.example.usercore.service

import com.example.usercore.dto.UserEventPayload
import com.example.usercore.dto.UserEventRequest
import com.example.usercore.repository.UserEventJpaRepository
import com.example.usercore.util.toJson
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserEventService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val userEventJpaRepository: UserEventJpaRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(this::class.simpleName)
    private val topic = "user.v1"

    @Transactional
    fun saveUserEvent(userEventRequest: UserEventRequest) {
        val userEvent = userEventJpaRepository.save(userEventRequest.toEventEntity())
        log.info("save userEvent $userEventRequest")
        applicationEventPublisher.publishEvent(UserEventPayload(id = userEvent.id))
    }

    @Transactional
    fun publishUserEvent(userEventPayload: UserEventPayload) {
        val userEvent = userEventJpaRepository.findByIdOrNull(userEventPayload.id) ?: return
        val value = userEvent.event.toJson()
        kafkaTemplate.send(topic, value)
        log.info("published $topic, $value")
        userEvent.isPublished = true
    }
}
