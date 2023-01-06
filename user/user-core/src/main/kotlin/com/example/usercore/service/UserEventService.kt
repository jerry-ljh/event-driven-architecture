package com.example.usercore.service

import com.example.usercore.domain.UserEvent
import com.example.usercore.dto.UserEventPayload
import com.example.usercore.dto.UserEventRequest
import com.example.usercore.repository.UserEventRepository
import com.example.usercore.util.toJson
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class UserEventService(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val userEventRepository: UserEventRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(this::class.simpleName)
    private val topic = "user.v1"

    @Transactional
    fun saveUserEvent(userEventRequest: UserEventRequest) {
        val userEvent = userEventRepository.save(userEventRequest.toEventEntity())
        log.info("save userEvent $userEventRequest")
        applicationEventPublisher.publishEvent(UserEventPayload(id = userEvent.id))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun publishUserEvent(userEventPayload: UserEventPayload) {
        val userEvent = userEventRepository.findByIdOrNull(userEventPayload.id) ?: return
        val value = userEvent.event.toJson()
        kafkaTemplate.send(topic, value)
        log.info("published $topic, $value")
        userEvent.isPublished = true
    }

    @Transactional(readOnly = true)
    fun findNotPublishUserEvent(): List<UserEvent> {
        return userEventRepository.findNotPublishedUserEvent()
    }
}
