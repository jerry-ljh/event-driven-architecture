package com.example.usercore.domain

import javax.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0L,
    @Column(nullable = false) val userId: String,
    @Column(nullable = false) val name: String,
)
