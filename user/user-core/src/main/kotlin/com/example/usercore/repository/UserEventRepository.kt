package com.example.usercore.repository

import com.example.usercore.domain.QUserEvent.userEvent
import com.example.usercore.domain.UserEvent
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
class UserEventRepository(
    private val userEventJpaRepository: UserEventJpaRepository
) : QuerydslRepositorySupport(UserEvent::class.java), UserEventJpaRepository by userEventJpaRepository {

    @PersistenceContext
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    fun findNotPublishedUserEvent(): List<UserEvent> {
        return from(userEvent)
            .where(userEvent.isPublished.eq(false))
            .limit(100)
            .fetch()
    }
}
