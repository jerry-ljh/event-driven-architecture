package com.example.user.configuration

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.KafkaAdminClient
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class KafkaConfiguration {

    @Bean
    fun defaultKafkaTemplate(@Value("\${kafka.user.bootstrap-servers}") brokers: String): KafkaTemplate<String, String> {
        val producerConfig = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to brokers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
        )
        return KafkaTemplate(DefaultKafkaProducerFactory(producerConfig))
    }

    @Bean
    fun kafkaAdminClient(@Value("\${kafka.user.bootstrap-servers}") brokers: String): AdminClient =
        KafkaAdminClient.create(mapOf(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to brokers))
}
