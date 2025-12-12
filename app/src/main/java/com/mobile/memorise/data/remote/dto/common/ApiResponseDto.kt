package com.mobile.memorise.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseDto<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    // Handle error specifics jika ada
    val error: String? = null
)