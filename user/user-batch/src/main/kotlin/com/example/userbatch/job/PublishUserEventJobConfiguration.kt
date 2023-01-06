package com.example.userbatch.job

import com.example.usercore.dto.UserEventPayload
import com.example.usercore.service.UserEventService
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PublishUserEventJobConfiguration(
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory,
    private val userEventService: UserEventService
) {

    @Bean
    fun publishUserEventJob(): Job {
        return jobBuilderFactory["publishUserEventJob"]
            .start(publishUserStep())
            .incrementer(RunIdIncrementer())
            .build()
    }

    fun publishUserStep(): Step {
        return stepBuilderFactory["publishUserEventStep"]
            .tasklet { _, _ ->
                val notPublishedUserEventList = userEventService.findNotPublishUserEvent()
                if (notPublishedUserEventList.isEmpty()) return@tasklet RepeatStatus.FINISHED
                notPublishedUserEventList.forEach {
                    userEventService.publishUserEvent(UserEventPayload(it.id))
                }
                return@tasklet RepeatStatus.CONTINUABLE
            }
            .build()
    }
}
