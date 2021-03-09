package com.csci4480.regiftcard.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.csci4480.regiftcard.R
import com.csci4480.regiftcard.data.classes.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class FirebaseMethods {
    companion object {
        private const val LOG_TAG = "448.ReGiftCard"
    }


    private lateinit var mDatabase: DatabaseReference

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mAuthListener: AuthStateListener
    private lateinit var mStorageReference: StorageReference

    private var mPhotoUploadProgress = 0.0

    private var userID: String? = null

    private var mActivity: Activity? = null
    private var mContext: Context? = null

    private var firebase_url: Uri? = null

    constructor(activity: Activity?, context: Context?) {
        mActivity = activity
        mContext = context
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference
        mStorageReference = FirebaseStorage.getInstance().getReference()
        if (mAuth.currentUser != null) {
            userID = mAuth.currentUser!!.uid
        }
    }

    fun FirebaseMethods(fragment: Fragment?, context: Context?) {}

    fun updateLikes(increment: Boolean, eventLikeCount: DatabaseReference) {
        eventLikeCount.runTransaction(object : Transaction.Handler {
            @NonNull
            override fun doTransaction(@NonNull mutableData: MutableData): Transaction.Result {
                if (mutableData.value != null) {
                    var value: Int = mutableData.getValue(Int::class.java)!!
                    if (increment) {
                        value++
                    } else {
                        value--
                    }
                    mutableData.value = value
                }
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                @Nullable databaseError: DatabaseError?,
                b: Boolean,
                @Nullable dataSnapshot: DataSnapshot?
            ) {
                Log.d(LOG_TAG, "likeTransaction:onComplete:$databaseError")
            }
        })
    }

    //Register new email, password, and username to Firebase Authentication
    fun registerNewEmail(mAuth: FirebaseAuth,
        username: String?,
        email: String?,
        password: String?,
        progressBar: ProgressBar
    ) {
        Log.d(LOG_TAG, "registerNewEmail: Attempting to register new user.")
        mAuth.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(
                mActivity!!
            ) { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Log.d(LOG_TAG, "registerNewEmail:success")
                    val user = mAuth.currentUser
                    val profile_update =
                            UserProfileChangeRequest.Builder().setDisplayName(username).build()
                    user!!.updateProfile(profile_update)
                    userID = mAuth.currentUser!!.uid
                    sendVerificationEmail()
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            mContext, "Authentication failed. Email address is already in use.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // If sign in fails, display a message to the user.
                    Log.w(LOG_TAG, "registerNewEmail:failure", task.exception)
                }
            }
    }

    fun signInWithEmail(email: String?, password: String?, progressBar: ProgressBar) {
        Log.d(LOG_TAG, "signInWithEmail: Attempting to sign in user.")
        mAuth.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(
                mActivity!!
            ) { task ->
                val user = mAuth.currentUser

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                progressBar.visibility = View.GONE
                if (!task.isSuccessful) {
                    // there was an error
                    Log.d(LOG_TAG, "signInWithEmail: Sign in unsuccessful.")
                    Toast.makeText(
                        mContext,
                        "Authentication failed, check your email and password or sign up.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Log.d(LOG_TAG, "signInWithEmail: Sign in successful.")
                }
            }
    }

    fun sendVerificationEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener(object : OnCompleteListener<Void?> {
            override fun onComplete(@NonNull task: Task<Void?>) {
                if (!task.isSuccessful) {
                    Toast.makeText(
                        mContext,
                        "Could not send verification email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    fun isUsernameAvailable(username: String, dataSnapshot: DataSnapshot): Boolean {
        Log.d(LOG_TAG, "isUsernameTaken: checking if $username already exists.")
        val user = User()
        for (ds: DataSnapshot in dataSnapshot.child("users").children) {
            Log.d(LOG_TAG, "isUsernameTaken: datasnapshot: $ds")
            user.username = ds.getValue(User::class.java)?.username
            Log.d(
                LOG_TAG, "isUsernameTaken: us" +
                        "ername is " + user.username
            )
            if (user.username.equals(username)) {
                Log.d(LOG_TAG, "isUsernameTaken: FOUND A MATCH: " + user.username)
                return false
            }
        }
        return true
    }

    fun isValidDomain(email: String): Boolean {
        if (!email.endsWith("@mymail.mines.edu") || !email.endsWith("@mines.edu")) {
            FirebaseAuth.getInstance().currentUser!!.delete()
            return false
        }
        return true
    }

    fun addNewUser(
        username: String,
        userID: String,
        email_address: String?,
        profile_photo: String
    ) {
        val filePaths = FilePaths()

        //photo being added is for profile picture
        val storageReference: StorageReference
        storageReference = mStorageReference.child(
            filePaths.FIREBASE_IMAGE_STORAGE
                .toString() + FirebaseAuth.getInstance().currentUser!!.uid + "/profile_photo"
        )
        var bm: Bitmap?
        //convert image url to bitmap
        if ((profile_photo == "")) {
            bm = BitmapFactory.decodeResource(
                mContext!!.resources,
                R.drawable.ic_default_profile_picture
            )
        } else {
            bm = ImageManager.getBitmap(profile_photo)
        }

        lateinit var bytes: ByteArray
        if (bm != null) {
            bytes = ImageManager.getBytesFromBitmap(bm, 100)
        } else {
            Log.e(LOG_TAG, "addNewUser(): Could not create Bitmap")
        }
        var uploadTask: UploadTask? = null
        uploadTask = storageReference.putBytes(bytes)
        uploadTask.addOnSuccessListener(OnSuccessListener<Any?> {
            storageReference.getDownloadUrl().addOnSuccessListener(OnSuccessListener<Uri?> { uri ->
                firebase_url = uri
                val user =
                    User(username, userID, email_address, firebase_url.toString())
                mDatabase!!.child("users").child(userID).setValue(user)
                Log.d(LOG_TAG, "addNewUser: Successfully added new user to database.")

                //input logic here to update all user adapter_event profile pictures
            })
            Toast.makeText(mContext, "Photo upload successful.", Toast.LENGTH_SHORT).show()

            //add new photo to 'user_events' node and 'user_events' node

            //navigate to main feed so user can see their photo
        }).addOnFailureListener(OnFailureListener {
            Log.d(LOG_TAG, "onFailure: Photo upload failed.")
            Toast.makeText(mContext, "Photo upload failed.", Toast.LENGTH_SHORT).show()
        }).addOnProgressListener { taskSnapshot ->
            val progress: Double =
                ((100 * taskSnapshot.getBytesTransferred()).toDouble() / taskSnapshot.getTotalByteCount())
            if (progress - 25 > mPhotoUploadProgress) {
                Toast.makeText(
                    mContext,
                    "photo upload progress: " + String.format("%.0f", progress),
                    Toast.LENGTH_SHORT
                ).show()
                mPhotoUploadProgress = progress
            }
            Log.d(LOG_TAG, "onProgress: upload progress: $progress% done")
        }
        Log.d(LOG_TAG, "addNewUser: Adding user $username , userID: $userID")
    }

    fun getImageCount(dataSnapshot: DataSnapshot): Int {
        var count = 0
        Log.d(LOG_TAG, "user id is " + FirebaseAuth.getInstance().currentUser!!.uid)
        for (ds: DataSnapshot? in dataSnapshot.children) {
            count++
        }
        return count
    }

    fun uploadNewProfilePicture(img_url: String?) {
        val filePaths = FilePaths()

        //photo being added is for profile picture
        val storageReference: StorageReference = mStorageReference.child(
            (filePaths.FIREBASE_IMAGE_STORAGE
                .toString() + FirebaseAuth.getInstance().currentUser!!.uid + "/profile_photo")
        )

        //convert image url to bitmap
        var bm: Bitmap? = null
        lateinit var bytes: ByteArray
        if (bm != null) {
            bytes = ImageManager.getBytesFromBitmap(bm, 100)
        } else {
            Log.e(LOG_TAG, "addNewUser(): Could not create Bitmap")
        }
        var uploadTask: UploadTask? = null
        uploadTask = storageReference.putBytes(bytes)
        uploadTask.addOnSuccessListener(OnSuccessListener<Any?> {
            storageReference.getDownloadUrl().addOnSuccessListener(OnSuccessListener<Uri?> { uri ->
                firebase_url = uri
                updateProfilePicture(firebase_url.toString())
                //input logic here to update all user adapter_event profile pictures
            })
            Toast.makeText(mContext, "Photo upload successful.", Toast.LENGTH_SHORT).show()

            //add new photo to 'user_events' node and 'user_events' node

            //navigate to main feed so user can see their photo
        }).addOnFailureListener(OnFailureListener {
            Log.d(LOG_TAG, "onFailure: Photo upload failed.")
            Toast.makeText(mContext, "Photo upload failed.", Toast.LENGTH_SHORT).show()
        }).addOnProgressListener(object : OnProgressListener<UploadTask.TaskSnapshot?> {
            override fun onProgress(taskSnapshot: UploadTask.TaskSnapshot) {
                val progress: Double =
                    ((100 * taskSnapshot.getBytesTransferred()).toDouble() / taskSnapshot.getTotalByteCount())
                if (progress - 25 > mPhotoUploadProgress) {
                    Toast.makeText(
                        mContext,
                        "photo upload progress: " + String.format("%.0f", progress),
                        Toast.LENGTH_SHORT
                    ).show()
                    mPhotoUploadProgress = progress
                }
                Log.d(LOG_TAG, "onProgress: upload progress: $progress% done")
            }
        })
    }
/*
    fun uploadNewEventWithPhoto(count: Int, img_url: String?, event: Event) {
        Log.d(LOG_TAG, "uploadNewPhoto: Attempting to upload new photo.")
        val filePaths = FilePaths()

        //photo being added is for adapter_event
        val storageReference: StorageReference = mStorageReference.child(
            (filePaths.FIREBASE_IMAGE_STORAGE
                .toString() + FirebaseAuth.getInstance().currentUser!!.uid + "/photo" + (count + 1))
        )

        //convert image url to bitmap
        var bm: Bitmap? = null
        lateinit var bytes: ByteArray
        if (bm != null) {
            bytes = ImageManager.getBytesFromBitmap(bm, 100)
        } else {
            Log.e(LOG_TAG, "addNewUser(): Could not create Bitmap")
        }
        var uploadTask: UploadTask? = null
        uploadTask = storageReference.putBytes(bytes)
        uploadTask.addOnSuccessListener(OnSuccessListener<Any?> {
            storageReference.getDownloadUrl().addOnSuccessListener(OnSuccessListener<Uri?> { uri ->
                firebase_url = uri
                event.setEvent_photo(firebase_url.toString())
                event.writeNewEventToSchoolOfficial(mDatabase, event)
                event.writeNewEventToUserEvents(mDatabase, event, mAuth!!.currentUser!!.uid)
            })
            Toast.makeText(mContext, "Photo upload successful.", Toast.LENGTH_SHORT).show()

            //add new photo to 'user_events' node and 'user_events' node

            //navigate to main feed so user can see their photo
        }).addOnFailureListener(OnFailureListener {
            Log.d(LOG_TAG, "onFailure: Photo upload failed.")
            Toast.makeText(mContext, "Photo upload failed.", Toast.LENGTH_SHORT).show()
        }).addOnProgressListener(object : OnProgressListener<UploadTask.TaskSnapshot?> {
            override fun onProgress(taskSnapshot: UploadTask.TaskSnapshot) {
                val progress: Double =
                    ((100 * taskSnapshot.getBytesTransferred()).toDouble() / taskSnapshot.getTotalByteCount())
                if (progress - 25 > mPhotoUploadProgress) {
                    Toast.makeText(
                        mContext,
                        "photo upload progress: " + String.format("%.0f", progress),
                        Toast.LENGTH_SHORT
                    ).show()
                    mPhotoUploadProgress = progress
                }
                Log.d(LOG_TAG, "onProgress: upload progress: $progress% done")
            }
        })
    }
*/
    fun updateProfilePicture(img_url: String?) {
        mDatabase.child("users").child(mAuth.currentUser!!.uid).child("profile_photo")
            .setValue(img_url)
    }

    fun sendPasswordResetEmail(email : String) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                                mContext,
                                "We have sent you instructions to reset your password!",
                                Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                                mContext,
                                "Failed to send reset email!",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }
}