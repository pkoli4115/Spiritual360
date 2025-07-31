package com.hindu.pooja.util

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

object StorageHelper {
    private val storage: FirebaseStorage = Firebase.storage

    suspend fun uploadProfileImage(uid: String, imageUri: Uri): String? {
        return try {
            val ref = storage.reference.child("profile_images/$uid.jpg")
            ref.putFile(imageUri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
