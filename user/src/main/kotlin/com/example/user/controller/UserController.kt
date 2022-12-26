package com.example.user.controller

import com.example.user.dto.CreateUserRequest
import com.example.user.dto.CreateUserResponse
import com.example.user.service.UserService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun createUser(@RequestBody createUserRequest: CreateUserRequest): CreateUserResponse {
        val user = userService.createUserWithEvent(createUserRequest.userId, createUserRequest.userName)
        return CreateUserResponse(
            id = user.id,
            userId = user.userId,
            userName = user.name
        )
    }
}