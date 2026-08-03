package com.ackileo.telematics.utils
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class UploadStatus {
    data class Progress(val percentage: Int) : UploadStatus()
    data class Success(val downloadUrl: String) : UploadStatus()
    data class Error(val message: String) : UploadStatus()
}

@Singleton
class StorageManager @Inject constructor(
    private val storage: FirebaseStorage
) {
    private val storageRef = storage.reference

    /**
     * Uploads a file and returns a Flow to track progress
     */
    fun uploadFile(uri: Uri, folder: String, fileName: String): Flow<UploadStatus> = callbackFlow {
        val fileRef = storageRef.child("$folder/$fileName")
        val uploadTask = fileRef.putFile(uri)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val percent = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            trySend(UploadStatus.Progress(percent))
        }.addOnSuccessListener {
            fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                trySend(UploadStatus.Success(downloadUri.toString()))
                close()
            }
        }.addOnFailureListener {
            trySend(UploadStatus.Error(it.localizedMessage ?: "Upload failed"))
            close(it)
        }

        awaitClose { uploadTask.cancel() }
    }
}
