package com.example.userapi.controller

import com.example.usercore.dto.CreateUserRequest
import com.example.usercore.dto.CreateUserResponse
import com.example.usercore.service.UserService
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
