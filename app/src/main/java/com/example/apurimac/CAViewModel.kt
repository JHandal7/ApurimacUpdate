package com.example.apurimac

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.apurimac.data.COLLECTION_CHAT
import com.example.apurimac.data.COLLECTION_MESSAGES
import com.example.apurimac.data.COLLECTION_USER
import com.example.apurimac.data.ChatData
import com.example.apurimac.data.ChatUser
import com.example.apurimac.data.Event
import com.example.apurimac.data.Message
import com.example.apurimac.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import org.checkerframework.checker.units.qual.C
import java.lang.Exception
import java.util.Calendar
import java.util.UUID
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
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val inProgressChats = mutableStateOf(false)
    val chatMessages = mutableStateOf<List<Message>>((listOf()))
    val inProgressChatMessages = mutableStateOf(false)

    init {
        // auth.signOut()
        //onLogout()
        //onLogin("yusuf@gmail.com", "123456" )
//onSignup("nadia","666666","nadia@gmail.com","123456")
        val currentUser = auth.currentUser
        signedIn.value = currentUser != null
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
                } else
                handleException(customMessage = "number already exists")
            inProgress.value = false
        }

            .addOnFailureListener {
                handleException(it)
            }
    }

    fun onLogin(email: String, password: String) {

        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please fill in all fields")
            return
        }
        inProgress.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    signedIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {

                        getUserData(it)
                    }
                } else handleException(task.exception, "Login failed")
            }
            .addOnFailureListener { handleException(it, "Login failure") }
    }

    private fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageUrl: String? = null

    ) {
        val uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            number = number ?: userData.value?.number,
            imageUrl = imageUrl ?: userData.value?.imageUrl
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

    fun updateProfileData(name: String, number: String) {
        createOrUpdateProfile(name = name, number = number)

    }

    private fun getUserData(uid: String) {
        inProgress.value = true
        db.collection(COLLECTION_USER).document(uid).addSnapshotListener { value, error ->
            if (error != null) handleException(error, "Cannot retrieve user data")
            if (value != null) {
                val user = value.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                populateChats()

            }
        }

    }

    fun onLogout() {
        auth.signOut()
        signedIn.value = false
        userData.value = null
        popupNotification.value = Event("Loged out")
        chats.value = listOf()

    }


    private fun handleException(
        exception: Exception? = null,
        customMessage: String = ""
    ) {
        Log.e("ChatAppClone", "chat app exception ", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: ""
        val message = if (customMessage.isEmpty())
            errorMsg else "$customMessage:$errorMsg"
        popupNotification.value = Event(message)
        inProgress.value = false
    }

    private fun uploadImage(uri: Uri, onSuccsses: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("image/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask
            .addOnSuccessListener {
                val result = it.metadata?.reference?.downloadUrl
                result?.addOnSuccessListener(onSuccsses)
                inProgress.value = false
            }
            .addOnFailureListener { handleException(it) }
    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri) {
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() or !number.isDigitsOnly())
            handleException(customMessage = "Numbers must contain only digits")
        else {
            db.collection(COLLECTION_CHAT)
                .where(
                    Filter.or(
                        Filter.and(
                            Filter.equalTo(
                                "user1.number", number
                            ),
                            Filter.equalTo("user2.number", userData.value?.number),
                            Filter.and(
                                Filter.equalTo(
                                    "user1.number", userData.value?.number
                                ),
                                Filter.equalTo("user2.number", number)

                            )
                        )

                    )//or
                )//where
                .get().addOnSuccessListener {
                    if (it.isEmpty) {
                        //add chat
                        db.collection(COLLECTION_USER).whereEqualTo("number", number)
                            .get()
                            .addOnSuccessListener {
                                if (it.isEmpty)
                                    handleException(customMessage = "Cannot retrieve user with number $number")
                                else {
                                    val chatPartner = it.toObjects<UserData>()[0]
                                    val id = db.collection(COLLECTION_CHAT).document().id
                                    val chat = ChatData(
                                        id,
                                        ChatUser(
                                            userData.value?.userId,
                                            userData.value?.name,
                                            userData.value?.imageUrl,
                                            userData.value?.number
                                        ),
                                        ChatUser(
                                            chatPartner.userId,
                                            chatPartner.name,
                                            chatPartner.imageUrl,
                                            chatPartner.number
                                        )
                                    )//chat
                                    db.collection(COLLECTION_CHAT).document(id).set(chat)
                                }
                            }
                            .addOnFailureListener {
                                handleException(it)
                            }
                    } else {
                        handleException(customMessage = "Chat already exists")
                    }
                }
        }//db
    }

    private fun populateChats() {
        inProgress.value = true
        db.collection(COLLECTION_CHAT).where(
            Filter.or(
                Filter.equalTo(
                    "user1.userId ", userData.value?.userId
                ),
                Filter.equalTo(
                    "user2.userId", userData.value?.userId

                )
            )
        )
            .addSnapshotListener { value, error ->
                if (
                    error != null
                ) handleException(error)
                if (value != null)
                    chats.value = value.documents.mapNotNull { it.toObject<ChatData>() }
                inProgress.value = false
            }//addSnapshotListener

    }

    fun onSendReplay(
        chatId: String, message: String
    ) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.userId, message, time)
        db.collection(COLLECTION_CHAT)
            .document(chatId)
            .collection(COLLECTION_MESSAGES)
            .document()
            .set(msg)

    }
}
