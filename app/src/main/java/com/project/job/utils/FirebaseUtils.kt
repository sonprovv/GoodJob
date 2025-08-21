package com.project.job.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for Firebase operations
 */
@Singleton
class FirebaseUtils @Inject constructor(
    val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    
    // Get current user ID, returns null if not signed in
    val currentUserId: String?
        get() = auth.currentUser?.uid
    
    // Check if user is signed in
    val isUserSignedIn: Boolean
        get() = auth.currentUser != null
    
    // Get reference to Realtime Database users node
    val usersReference: DatabaseReference
        get() = database.reference.child("users")
    
    // Get reference to Firestore users collection
    val usersCollection
        get() = firestore.collection("users")
    
    // Get reference to Storage profile images
    val profileImagesReference
        get() = storage.reference.child("profile_images")
    
    // Sign out the current user
    fun signOut() {
        auth.signOut()
    }
    
    companion object {
        // Common database paths
        const val USERS_PATH = "users"
        const val JOBS_PATH = "jobs"
        const val APPLICATIONS_PATH = "applications"
    }
}
