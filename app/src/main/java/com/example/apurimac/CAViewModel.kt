package com.example.apurimac

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.apurimac.data.COLLECTION_USER
import com.example.apurimac.data.Event
import com.example.apurimac.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class CAViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    val inProgress = mutableStateOf(false)
    val popupNotification = mutableStateOf<Event<String>?>(null)
    val signedIn = mutableStateOf(value = false)
    val userData = mutableStateOf<UserData?>(null)

    init {

        val currentUser = auth.currentUser
        signedIn.value != null
        currentUser?.uid?.let { uid ->
            getUserData(uid)
        }

    }

    fun onSignup(name: String, number: String, email: String, password: String) {
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        db.collection(COLLECTION_USER).whereEqualTo("number", number).get().addOnSuccessListener {
            if (it.isEmpty)
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        signedIn.value = true
                        createOrUpdateProfile(name = name, number = number)
                    } else handleException(task.exception, "Signup failed")
                }
            handleException(customMessage = "number already exists")
            inProgress.value = false
        }

            .addOnFailureListener {
                handleException(it)
            }
    }

    fun onLogin(email: String, pass: String) {

        if (email.isEmpty() or pass.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                    task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {

                        getUserData(it)
                    }
                } else handleException(task.exception, "Login failed")
            }
            .addOnFailureListener{handleException(it,"Login failure")}
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageUrl: String? = null

    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name,
            number = number,
            imageUrl = imageUrl
        )
        uid?.let { uid ->
            inProgress.value = true
            db.collection(COLLECTION_USER).document(uid).get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        //Update user
                        it.reference.update(userData.toMap())
                            .addOnSuccessListener { inProgress.value = false }
                            .addOnFailureListener { handleException(it, "Can not update user") }
                    } else { //Create user
                        db.collection(COLLECTION_USER).document(uid).set(userData)
                        inProgress.value = false
                        getUserData(uid)
                    }

                }
                .addOnFailureListener { handleException(it, "Can not retrieve user") }
        }
    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(COLLECTION_USER).document(uid).addSnapshotListener { value, error ->
            if (error != null) handleException(error, "Cannot retrieve user data")
            if (value != null) {
                val user = value.toObject<UserData>()
                userData.value = user
                inProgress.value = false

            }
        }

    }

    private fun handleException(
        exception: Exception? = null,
        customMessage: String = ""
    ) {
        Log.e("ChatAppClone", "chat app exception ", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: null
        val message = if (customMessage.isEmpty())
            errorMsg else "$customMessage:$errorMsg"
        popupNotification.value = Event(message)
        inProgress.value = false
    }


}
