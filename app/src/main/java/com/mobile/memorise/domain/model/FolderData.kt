package com.mobile.memorise.domain.model

import com.google.gson.annotations.SerializedName

// Model khusus untuk update (biasanya sama dengan create, tapi dipisah agar fleksibel)
data class UpdateFolderRequest(
    val name: String,
    val color: String
)

// Input ke API
data class CreateFolderRequest(
    val name: String,
    val description: String = "Created via Mobile App", // Default description karena UI tidak memintanya
    val color: String
)

// Output Sukses dari API
data class FolderResponse(
    val success: Boolean,
    val message: String,
    val data: FolderData?
)

data class FolderListResponse(
    val success: Boolean,
    val message: String,
    val data: List<FolderData> = emptyList()
)

data class FolderData(
    @SerializedName("_id") val id: String,
    val name: String,
    val description: String?,
    val color: String,
    val userId: String,
    val createdAt: String
)

// Model untuk Error (ketika success = false)
data class ErrorResponse(
    val success: Boolean,
    val message: String,
    val errors: List<ValidationError>?
)

data class ValidationError(
    val field: String,
    val message: String
)