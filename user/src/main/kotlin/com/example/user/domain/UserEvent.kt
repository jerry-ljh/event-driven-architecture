package com.example.user.domain

import com.vladmihalcea.hibernate.type.json.JsonType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import javax.persistence.*

@Entity
@Table(name = "user_events")
@TypeDef(name = "json", typeClass = JsonType::class)
class UserEvent(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0L,
    @Type(type = "json") @Column(nullable = false, columnDefinition = "json") val event: Map<String, Any>,
    @Column(nullable = false) var isPublished: Boolean = false
)
