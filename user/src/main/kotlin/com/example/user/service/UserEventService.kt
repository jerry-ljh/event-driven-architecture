package com.example.user.service

import com.example.user.dto.UserEvent
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class UserEventService(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    private val log = LoggerFactory.getLogger(this::class.simpleName)
    private val topic = "user.v1"
    private val objectMapper = jacksonObjectMapper()

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun createUserEventWithTransactionalListener(userEvent: UserEvent){
        val value = objectMapper.writeValueAsString(userEvent)
        log.info("publish $topic, $value")
        kafkaTemplate.send(topic, value)
    }
}
