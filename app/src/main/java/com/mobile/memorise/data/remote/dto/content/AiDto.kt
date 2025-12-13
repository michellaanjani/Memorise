//package com.mobile.memorise.data.remote.dto.content
//
//import com.google.gson.annotations.SerializedName
//
//// --- WRAPPER RESPONSE (Gunakan ApiResponseDto yang sudah ada di common,
//// tapi jika backend AI return structure beda, pakai ini sementara) ---
//// Note: Sebaiknya gunakan ApiResponseDto yang sama dengan Home/Deck jika format JSON sama.
//
//// --- UPLOAD ---
//data class UploadResponseData(
//    @SerializedName("_id") val id: String,
//    val originalname: String,
//    val url: String?
//)
//
//// --- GENERATE ---
//data class AiGenerateRequest(
//    val fileId: String?,
//    val format: String,
//    val cardAmount: Int
//)
//
//data class AiGenerateResultData(
//    val deck: AiDeckInfo,
//    val cards: List<AiCard>
//)
//
//data class AiDeckInfo(
//    @SerializedName("_id") val id: String,
//    val name: String,
//    val description: String?,
//    val isDraft: Boolean
//)
//
//data class AiCard(
//    @SerializedName("_id") val id: String,
//    val front: String,
//    val back: String,
//    val deckId: String? = null
//)
//
//// --- DRAFT ---
//data class AiDraftDetailData(
//    @SerializedName("_id") val id: String,
//    val name: String,
//    val description: String?,
//    val isDraft: Boolean,
//    val cards: List<AiCard>
//)
//
//data class UpdateCardRequest(
//    val front: String,
//    val back: String
//)
//
//data class SaveDeckRequest(
//    val folderId: String? = null
//)
//
//// Tambahkan class ini agar error 'Unresolved reference: FileUrlDto' hilang
//data class FileUrlDto(
//    val url: String
//)