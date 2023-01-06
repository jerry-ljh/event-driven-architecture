package com.example.userbatch

import com.example.usercore.CoreConfigurationLoader
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@Import(CoreConfigurationLoader::class)
@EnableBatchProcessing
@SpringBootApplication
class UserBatchApplication

fun main(args: Array<String>) {
    runApplication<UserBatchApplication>(*args)
}
