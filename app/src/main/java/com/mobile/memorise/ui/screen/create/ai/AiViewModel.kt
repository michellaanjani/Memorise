package com.mobile.memorise.ui.screen.create.ai

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.memorise.domain.model.AiDraftContent
import com.mobile.memorise.domain.model.AiGeneratedContent
import com.mobile.memorise.domain.model.UploadedFile
import com.mobile.memorise.domain.repository.ContentRepository
import com.mobile.memorise.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AiViewModel @Inject constructor(
    private val repository: ContentRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // --- STATES ---
    private val _uploadState = MutableStateFlow<Resource<UploadedFile>>(Resource.Idle())
    val uploadState: StateFlow<Resource<UploadedFile>> = _uploadState.asStateFlow()

    private val _generateState = MutableStateFlow<Resource<AiGeneratedContent>>(Resource.Idle())
    val generateState: StateFlow<Resource<AiGeneratedContent>> = _generateState.asStateFlow()

    private val _draftState = MutableStateFlow<Resource<AiDraftContent>>(Resource.Idle())
    val draftState: StateFlow<Resource<AiDraftContent>> = _draftState.asStateFlow()

    private val _saveState = MutableStateFlow<Resource<Boolean>>(Resource.Idle())
    val saveState: StateFlow<Resource<Boolean>> = _saveState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<Boolean>>(Resource.Idle())
    val deleteState: StateFlow<Resource<Boolean>> = _deleteState.asStateFlow()

    private val _currentDeckName = MutableStateFlow("")
    val currentDeckName: StateFlow<String> = _currentDeckName.asStateFlow()

    fun updateDeckNameLocal(name: String) {
        _currentDeckName.value = name
    }

    fun resetStates() {
        _uploadState.value = Resource.Idle()
        _generateState.value = Resource.Idle()
        _draftState.value = Resource.Idle()
        _saveState.value = Resource.Idle()
        _deleteState.value = Resource.Idle()
    }

    fun resetDeleteState() {
        _deleteState.value = Resource.Idle()
    }

    // =================================================================
    // 1. UPLOAD & GENERATE
    // =================================================================

    fun uploadAndGenerate(
        contentResolver: ContentResolver,
        uri: Uri,
        mimeType: String?,
        fileName: String? = null,
        cardAmountInput: String,
        formatInput: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uploadState.value = Resource.Loading()
            _generateState.value = Resource.Idle()

            val finalCardAmount = cardAmountInput.toIntOrNull()?.coerceIn(2, 40) ?: 5
            val finalFormat = if (formatInput.contains("Question", true)) "question" else "definition"

            try {
                val tempFile = createTempFileFromUri(contentResolver, uri, mimeType, fileName)
                    ?: throw Exception("Gagal memproses file")

                val uploadResult = repository.uploadFile(tempFile)
                val fileId = uploadResult.getOrNull()?.id
                if (tempFile.exists()) tempFile.delete()

                if (uploadResult.isSuccess && fileId != null) {
                    _uploadState.value = Resource.Success(uploadResult.getOrThrow())
                    generateInternal(fileId, finalFormat, finalCardAmount)
                } else {
                    val errorMsg = uploadResult.exceptionOrNull()?.message ?: "Upload gagal"
                    _uploadState.value = Resource.Error(errorMsg)
                }

            } catch (e: Exception) {
                _uploadState.value = Resource.Error(e.localizedMessage ?: "Terjadi kesalahan sistem")
            }
        }
    }

    private suspend fun generateInternal(fileId: String, format: String, cardAmount: Int) {
        _generateState.value = Resource.Loading()
        try {
            val result = repository.generateFlashcards(
                prompt = format,
                fileId = fileId,
                cardAmount = cardAmount
            )

            result.onSuccess { data ->
                _generateState.value = Resource.Success(data)
            }.onFailure {
                _generateState.value = Resource.Error(it.message ?: "Gagal generate flashcard")
            }
        } catch (e: Exception) {
            _generateState.value = Resource.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan")
        }
    }

    // =================================================================
    // 2. DRAFT & CARDS MANAGEMENT
    // =================================================================

    fun loadDraft(deckId: String) {
        viewModelScope.launch {
            _draftState.value = Resource.Loading()
            try {
                val result = repository.getAiDraft(deckId)
                result.onSuccess { content ->
                    _draftState.value = Resource.Success(content)
                    if (_currentDeckName.value.isEmpty()) {
                        _currentDeckName.value = content.deck.name
                    }
                }.onFailure {
                    _draftState.value = Resource.Error(it.message ?: "Gagal memuat draft")
                }
            } catch (e: Exception) {
                _draftState.value = Resource.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan")
            }
        }
    }

    fun updateDraftCard(deckId: String, cardId: String, front: String, back: String) {
        viewModelScope.launch {
            try {
                val result = repository.updateDraftCard(deckId, cardId, front, back)
                result.onSuccess { content ->
                    _draftState.value = Resource.Success(content)
                }
            } catch (e: Exception) {
                // Silent fail
            }
        }
    }

    fun deleteDraftCard(deckId: String, cardId: String) {
        viewModelScope.launch {
            _deleteState.value = Resource.Loading()
            try {
                val result = repository.deleteDraftCard(deckId, cardId)

                result.onSuccess { _ ->
                    val currentState = _draftState.value
                    if (currentState is Resource.Success) {
                        // 🔥 PERBAIKAN: Elvis Operator (?: return@launch)
                        // Jika data null, hentikan eksekusi agar tidak crash.
                        val currentData = currentState.data ?: return@launch

                        // Filter manual untuk UI update instan
                        val updatedCards = currentData.cards.filter { it.id != cardId }

                        val updatedContent = currentData.copy(
                            cards = updatedCards,
                            deck = currentData.deck.copy(cardCount = updatedCards.size)
                        )
                        _draftState.value = Resource.Success(updatedContent)
                    }
                    _deleteState.value = Resource.Success(true)
                }.onFailure {
                    _deleteState.value = Resource.Error(it.message ?: "Gagal menghapus")
                }
            } catch (e: Exception) {
                _deleteState.value = Resource.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    fun saveDraft(deckId: String, folderId: String?, newName: String?) {
        viewModelScope.launch {
            if (deckId.isBlank()) {
                _saveState.value = Resource.Error("Gagal menyimpan: ID Deck tidak valid")
                return@launch
            }

            _saveState.value = Resource.Loading()
            try {
                val currentDesc = (_draftState.value as? Resource.Success)?.data?.deck?.description ?: ""

                if (!newName.isNullOrBlank()) {
                    val updateResult = repository.updateDeck(
                        id = deckId,
                        name = newName,
                        desc = currentDesc,
                        folderId = null
                    )

                    if (updateResult.isFailure) {
                        val errorMsg = updateResult.exceptionOrNull()?.message ?: "Gagal mengubah nama deck"
                        _saveState.value = Resource.Error(errorMsg)
                        return@launch
                    }
                }

                val saveResult = repository.saveAiDraft(deckId, folderId, null)

                saveResult.onSuccess {
                    _saveState.value = Resource.Success(true)
                }.onFailure {
                    _saveState.value = Resource.Error(it.message ?: "Gagal menyimpan deck")
                }

            } catch (e: Exception) {
                _saveState.value = Resource.Error(e.localizedMessage ?: "Terjadi kesalahan jaringan")
            }
        }
    }

    // =================================================================
    // 3. FILE HELPER FUNCTIONS
    // =================================================================

    private fun createTempFileFromUri(cr: ContentResolver, uri: Uri, mime: String?, name: String?): File? {
        val detectedMimeType = detectMimeType(uri, mime, name)
        val timestamp = System.currentTimeMillis()
        val shouldConvertToJpeg = detectedMimeType?.lowercase(Locale.getDefault())
            ?.let { it == "image/heic" || it == "image/heif" } ?: false

        val (payloadBytes, payloadMimeType) = if (shouldConvertToJpeg) {
            val jpegBytes = convertUriToJpegBytes(cr, uri) ?: return null
            jpegBytes to "image/jpeg"
        } else {
            val inputStream = cr.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()
            bytes to (detectedMimeType ?: "application/octet-stream")
        }

        val uploadFileName = if (shouldConvertToJpeg) {
            val baseName = name?.substringBeforeLast('.')?.takeIf { !it.isBlank() }
            if (baseName != null) "${baseName}.jpg" else "upload_${timestamp}.jpg"
        } else if (!name.isNullOrBlank()) {
            name
        } else {
            val extension = getFileExtension(uri, name) ?: payloadMimeType.let { getExtensionFromMimeType(it) }
            "upload_${timestamp}${if (!extension.isNullOrBlank()) ".$extension" else ""}"
        }

        val tempFile = File(context.cacheDir, uploadFileName)
        FileOutputStream(tempFile).use { it.write(payloadBytes) }
        return tempFile
    }

    private fun detectMimeType(uri: Uri, mimeType: String?, fileName: String?): String? {
        if (!mimeType.isNullOrBlank()) {
            val extensionFromMime = getExtensionFromMimeType(mimeType)
            val fileExtension = getFileExtension(uri, fileName)
            if (extensionFromMime != null && fileExtension != null &&
                extensionFromMime.equals(fileExtension, ignoreCase = true)) {
                return mimeType
            }
        }
        val fileExtension = getFileExtension(uri, fileName)
        if (fileExtension != null) {
            val mimeFromExtension = getMimeTypeFromExtension(fileExtension)
            if (mimeFromExtension != null) return mimeFromExtension
        }
        return mimeType ?: "application/octet-stream"
    }

    private fun getFileExtension(uri: Uri, fileName: String?): String? {
        if (!fileName.isNullOrBlank()) {
            val lastDot = fileName.lastIndexOf('.')
            if (lastDot >= 0 && lastDot < fileName.length - 1) {
                return fileName.substring(lastDot + 1).lowercase(Locale.getDefault())
            }
        }
        val path = uri.path
        if (!path.isNullOrBlank()) {
            val lastDot = path.lastIndexOf('.')
            if (lastDot >= 0 && lastDot < path.length - 1) {
                return path.substring(lastDot + 1).lowercase(Locale.getDefault())
            }
        }
        return null
    }

    private fun getMimeTypeFromExtension(extension: String): String? {
        return when (extension.lowercase(Locale.getDefault())) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            "heif" -> "image/heif"
            "txt" -> "text/plain"
            "rtf" -> "application/rtf"
            "odt" -> "application/vnd.oasis.opendocument.text"
            else -> null
        }
    }

    private fun getExtensionFromMimeType(mimeType: String): String? {
        return when (mimeType.lowercase(Locale.getDefault())) {
            "application/pdf" -> "pdf"
            "application/msword" -> "doc"
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/bmp" -> "bmp"
            "image/webp" -> "webp"
            "image/heic" -> "heic"
            "image/heif" -> "heif"
            "text/plain" -> "txt"
            "application/rtf" -> "rtf"
            "application/vnd.oasis.opendocument.text" -> "odt"
            else -> null
        }
    }

    private fun convertUriToJpegBytes(contentResolver: ContentResolver, uri: Uri): ByteArray? {
        val bitmap = decodeBitmapFromUri(contentResolver, uri) ?: return null
        val baos = ByteArrayOutputStream()
        val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        if (!ok) return null
        return baos.toByteArray()
    }

    private fun decodeBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                val inputStream = contentResolver.openInputStream(uri) ?: return null
                inputStream.use { BitmapFactory.decodeStream(it) }
            }
        } catch (e: Exception) {
            null
        }
    }
}