package com.example.user.service

import com.example.user.domain.User
import com.example.user.domain.UserEvent
import com.example.user.dto.Action
import com.example.user.dto.UserEventPayload
import com.example.user.dto.UserEventRequest
import com.example.user.repository.UserEventJpaRepository
import com.example.user.repository.UserJpaRepository
import com.example.user.util.toJson
import com.example.user.util.toMap
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.streams.toList

@SpringBootTest
@RecordApplicationEvents
class UserEventServiceTest {

    @Autowired
    lateinit var sut: UserEventService

    @Autowired
    lateinit var events: ApplicationEvents

    @SpykBean
    lateinit var userJpaRepository: UserJpaRepository

    @SpykBean
    lateinit var userEventJpaRepository: UserEventJpaRepository

    @MockkBean(relaxed = true)
    lateinit var kafkaTemplate: KafkaTemplate<String, String>

    @BeforeEach
    fun clean() {
        userJpaRepository.deleteAllInBatch()
        userEventJpaRepository.deleteAllInBatch()
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
        val userEvent = userEventJpaRepository.findAll().first()
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
        val userEvent = userEventJpaRepository.save(UserEvent(event = userEventRequest.toMap()))
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
        val userEvent = userEventJpaRepository.save(UserEvent(event = userEventRequest.toMap()))
        val userEventPayload = UserEventPayload(id = userEvent.id)
        // when
        sut.publishUserEvent(userEventPayload)
        // then
        verify { kafkaTemplate.send(any(), userEvent.event.toJson()) }
        userEventJpaRepository.findByIdOrNull(userEvent.id)!!.isPublished shouldBe true
    }
}
