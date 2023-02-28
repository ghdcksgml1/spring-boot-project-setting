package com.proseed.api.user.repository

import com.proseed.api.user.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Int> {

    fun findByPlatformId(platformId: String): User?

}