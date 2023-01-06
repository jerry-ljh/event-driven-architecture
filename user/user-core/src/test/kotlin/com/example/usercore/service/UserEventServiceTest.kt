package com.example.usercore.service

import com.example.usercore.TestConfig
import com.example.usercore.domain.User
import com.example.usercore.domain.UserEvent
import com.example.usercore.dto.Action
import com.example.usercore.dto.UserEventPayload
import com.example.usercore.dto.UserEventRequest
import com.example.usercore.repository.UserEventRepository
import com.example.usercore.repository.UserJpaRepository
import com.example.usercore.util.toJson
import com.example.usercore.util.toMap
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.streams.toList

@RecordApplicationEvents
class UserEventServiceTest : TestConfig() {

    @Autowired
    lateinit var sut: UserEventService

    @Autowired
    lateinit var events: ApplicationEvents

    @SpykBean
    lateinit var userJpaRepository: UserJpaRepository

    @SpykBean
    lateinit var userEventRepository: UserEventRepository

    @MockkBean(relaxed = true)
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @BeforeEach
    fun clean() {
        userJpaRepository.deleteAllInBatch()
        userEventRepository.deleteAllInBatch()
        clearAllMocks()
    }

    @Test
    fun `생성된 이벤트 정보를 isPublished=false로 저장한다`() {
        // given
        val user = userJpaRepository.save(User(userId = "jerry", name = "lee"))
        val userEventRequest = UserEventRequest(
            id = user.id,
            action = Action.CREATE
        )
        // when
        sut.saveUserEvent(userEventRequest)
        // then
        val userEvent = userEventRepository.findAll().first()
        userEvent.isPublished shouldBe false
        userEvent.event["id"] shouldBe userEventRequest.id
        userEvent.event["action"] shouldBe userEventRequest.action.name
    }

    @Test
    fun `생성된 이벤트 정보를 저장했다면 kafka 이벤트 발행 요청을 생성한다`() {
        // given
        val user = userJpaRepository.save(User(userId = "jerry", name = "lee"))
        val userEventRequest = UserEventRequest(
            id = user.id,
            action = Action.CREATE
        )
        // when
        sut.saveUserEvent(userEventRequest)
        // then
        val events = events.stream(UserEventPayload::class.java).toList()
        events shouldHaveSize 1
    }

    @Test
    fun `kafka 이벤트를 발행한다`() {
        // given
        val userEventRequest = UserEventRequest(id = 1, action = Action.CREATE)
        val userEvent = userEventRepository.save(UserEvent(event = userEventRequest.toMap()))
        val userEventPayload = UserEventPayload(id = userEvent.id)
        // when
        sut.publishUserEvent(userEventPayload)
        // then
        verify { kafkaTemplate.send(any(), userEvent.event.toJson()) }
    }

    @Test
    fun `kafka 이벤트 발행에 성공하면 userEvent isPublished=true로 저장한다`() {
        // given
        val userEventRequest = UserEventRequest(id = 1, action = Action.CREATE)
        val userEvent = userEventRepository.save(UserEvent(event = userEventRequest.toMap()))
        val userEventPayload = UserEventPayload(id = userEvent.id)
        // when
        sut.publishUserEvent(userEventPayload)
        // then
        verify { kafkaTemplate.send(any(), userEvent.event.toJson()) }
        userEventRepository.findByIdOrNull(userEvent.id)!!.isPublished shouldBe true
    }
}
