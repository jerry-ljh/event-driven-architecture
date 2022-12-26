package com.example.coupon

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class UserConsumer {

    private val log = LoggerFactory.getLogger(this::class.simpleName)
    private val objectMapper = jacksonObjectMapper()

    @KafkaListener(
        topics = ["user.v1"],
        concurrency = "1",
    )
    fun consumerListen(records: List<ConsumerRecord<String, String>>) {
        val userEvents = records.map { objectMapper.readValue(it.value(), UserEvent::class.java) }
        log.info(userEvents.toString())
        userEvents.forEach {
            if (it.action == Action.CREATE) {
                log.info("신규 유저 쿠폰 발급 userId: ${it.id}")
            }
        }
    }
}

data class UserEvent(
    val id: Long,
    val action: Action
)

enum class Action {
    CREATE, UPDATE
}
