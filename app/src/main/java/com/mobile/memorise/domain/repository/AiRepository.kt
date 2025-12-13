//package com.mobile.memorise.domain.repository
//
//import android.content.Context
//import android.net.Uri
//import com.mobile.memorise.data.remote.api.ApiService
//import com.mobile.memorise.data.remote.dto.content.AiCard
//import com.mobile.memorise.data.remote.dto.content.AiDeckInfo
//import com.mobile.memorise.data.remote.dto.content.AiDraftDetailData
//import com.mobile.memorise.data.remote.dto.content.AiGenerateRequest
//import com.mobile.memorise.data.remote.dto.content.AiGenerateResultData
//import com.mobile.memorise.data.remote.dto.content.SaveDeckRequest
//import com.mobile.memorise.data.remote.dto.content.UpdateCardRequest
//import com.mobile.memorise.data.remote.dto.content.UploadResponseData
//import com.mobile.memorise.util.getFileFromUri
//import dagger.hilt.android.qualifiers.ApplicationContext
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.RequestBody.Companion.asRequestBody
//import javax.inject.Inject
//
//class AiRepository @Inject constructor(
//    private val api: ApiService,
//    @ApplicationContext private val context: Context // Tambahkan @ApplicationContext
//) {
//
//    suspend fun uploadFile(uri: Uri): Result<UploadResponseData> {
//        return try {
//            // Helper function getFileFromUri harus ada di package util
//            val file = getFileFromUri(context, uri) ?: return Result.failure(Exception("File not found or cannot be read"))
//
//            val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
//            val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
//            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
//
//            val response = api.uploadFile(body)
//
//            // Cek ApiResponseDto (success dan data tidak null)
//            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
//                Result.success(response.body()!!.data!!)
//            } else {
//                val msg = response.body()?.message ?: response.errorBody()?.string() ?: "Upload failed"
//                Result.failure(Exception(msg))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun generateFlashcards(fileId: String, format: String, amount: Int): Result<AiGenerateResultData> {
//        return try {
//            val request = AiGenerateRequest(fileId, format, amount)
//            val response = api.generateFlashcards(request)
//
//            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
//                Result.success(response.body()!!.data!!)
//            } else {
//                val msg = response.body()?.message ?: "Generation failed"
//                Result.failure(Exception(msg))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun getDraft(deckId: String): Result<AiDraftDetailData> {
//        return try {
//            val response = api.getDraftDeck(deckId)
//            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
//                Result.success(response.body()!!.data!!)
//            } else {
//                Result.failure(Exception("Failed to load draft"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun updateCard(deckId: String, cardId: String, front: String, back: String): Result<AiCard> {
//        return try {
//            val response = api.updateDraftCard(deckId, cardId, UpdateCardRequest(front, back))
//            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
//                Result.success(response.body()!!.data!!)
//            } else {
//                Result.failure(Exception("Update failed"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun deleteCard(deckId: String, cardId: String): Result<Unit> {
//        return try {
//            val response = api.deleteDraftCard(deckId, cardId)
//            if (response.isSuccessful && response.body()?.success == true) {
//                Result.success(Unit)
//            } else {
//                Result.failure(Exception("Delete failed"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//    suspend fun saveDeck(deckId: String): Result<AiDeckInfo> {
//        return try {
//            val response = api.saveDeck(deckId, SaveDeckRequest(null))
//            if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
//                Result.success(response.body()!!.data!!)
//            } else {
//                Result.failure(Exception("Save failed"))
//            }
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//}