package com.mobile.memorise.domain.repository

import com.mobile.memorise.domain.model.EmailVerificationStatus

interface UserRepository {
    suspend fun getEmailVerificationStatus(): EmailVerificationStatus
}
