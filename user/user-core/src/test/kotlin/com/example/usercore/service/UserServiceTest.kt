package com.example.usercore.service

import com.example.usercore.TestConfig
import com.example.usercore.dto.Action
import com.example.usercore.dto.UserEventRequest
import com.example.usercore.repository.UserEventRepository
import com.example.usercore.repository.UserJpaRepository
import com.example.usercore.util.toMap
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import kotlin.streams.toList

@RecordApplicationEvents
class UserServiceTest : TestConfig() {

    @Autowired
    lateinit var sut: UserService

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
    fun `유저 생성에 성공하면 생성 이벤트를 발행한다`() {
        // when
        sut.createUserWithEvent(userId = "jerry", userName = "lee")
        // then
        val events = events.stream(UserEventRequest::class.java).toList()
        events shouldHaveSize 1
        events.first().action shouldBe Action.CREATE
    }

    @Test
    fun `유저 생성에 성공하면 생성 이벤트도 같이 저장된다`() {
        // when
        sut.createUserWithEvent(userId = "jerry", userName = "lee")
        // then
        val eventRequest = events.stream(UserEventRequest::class.java).toList().first()
        val event = userEventRepository.findAll().first().event
        event shouldBe eventRequest.toMap()
    }

    @Test
    fun `유저 저장에 실패하면 유저 생성 이벤트를 발행하지 않는다`() {
        // givne
        every { userJpaRepository.save(any()) } throws RuntimeException("유저 저장 실패")
        // when
        assertThrows<RuntimeException> {
            sut.createUserWithEvent(userId = "jerry", userName = "lee")
        }
        // then
        val events = events.stream(UserEventRequest::class.java).toList()
        events shouldHaveSize 0
    }

    @Test
    fun `유저 이벤트 저장에 실패하면 유저 생성 트랜잭션이 롤백된다`() {
        // givne
        every { userEventRepository.save(any()) } throws RuntimeException("이벤트 저장 실패")
        // when
        assertThrows<RuntimeException> {
            sut.createUserWithEvent(userId = "jerry", userName = "lee")
        }
        // then
        userJpaRepository.findByUserId("jerry") shouldBe null
    }
}
