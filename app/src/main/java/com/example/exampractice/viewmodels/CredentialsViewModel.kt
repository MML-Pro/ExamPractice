package com.example.exampractice.viewmodels

import android.util.ArrayMap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exampractice.models.RankModel
import com.example.exampractice.models.User
import com.example.exampractice.util.RegisterValidation
import com.example.exampractice.util.Resource
import com.example.exampractice.util.validateEmail
import com.example.exampractice.util.validatePassword
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CredentialsViewModel"

@HiltViewModel
class CredentialsViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth, private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _register = MutableStateFlow<Resource<FirebaseUser>>(Resource.Ideal())
    val register: Flow<Resource<FirebaseUser>> get() = _register

    private var _validation = Channel<RegisterValidation.RegisterFailedStates>()
    val validation get() = _validation.receiveAsFlow()

    private val _login = MutableSharedFlow<Resource<FirebaseUser>>()
    val login get() = _login.asSharedFlow()

    private val _signInWithGoogle = MutableSharedFlow<Resource<GoogleSignInAccount>>()
    val signInWithGoogle get() = _signInWithGoogle.asSharedFlow()

    private val _updateUserInfo = MutableStateFlow<Resource<Boolean>>(Resource.Ideal())
    val updateUserInfo: Flow<Resource<Boolean>> get() = _updateUserInfo

    private var _myPerformance = MutableStateFlow<Resource<RankModel>>(Resource.Ideal())
    val myPerformance: Flow<Resource<RankModel>> get() = _myPerformance

    companion object {
        var myPerformanceLocal = RankModel(null, 0, -1)
    }


    private var _user = MutableStateFlow<Resource<User>>(Resource.Ideal())
    val user: Flow<Resource<User>> get() = _user


    //====================== SignUp code ======================//

    fun createAccountWithEmailAndPassword(
        user: User,
        password: String,
        confirmationPassword: String
    ) {

        if (checkValidation(user, password, confirmationPassword)) {
            viewModelScope.launch {
                _register.emit(Resource.Loading())
            }
            firebaseAuth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener { result ->

                    result.user?.let {
                        _register.value = Resource.Success(it)
                        saveUserInfo(it.uid, user.userName, user.email)
                    }

                }
                .addOnFailureListener {
                    _register.value = Resource.Error(it.message.toString())
                }
        } else {
            val registerFailedStates = RegisterValidation.RegisterFailedStates(
                validateEmail(user.email),
                validatePassword(password, confirmationPassword)
            )

            viewModelScope.launch {
                _validation.send(registerFailedStates)
            }
        }
    }

    private fun checkValidation(
        user: User,
        password: String,
        confirmationPassword: String
    ): Boolean {
        val emailValidation = validateEmail(user.email)

        val passwordValidation = validatePassword(password, confirmationPassword)

        return (emailValidation is RegisterValidation.Success
                && passwordValidation is RegisterValidation.Success)
    }

    private fun checkLoginCredentials(email: String, password: String): Boolean {
        val emailValidation = validateEmail(email)

        val passwordValidation = validatePassword(password)

        return (emailValidation is RegisterValidation.Success
                && passwordValidation is RegisterValidation.Success)
    }


    private fun saveUserInfo(userUID: String, userName: String, email: String) {

        val userData: ArrayMap<String, Any> = ArrayMap<String, Any>()

        userData["NAME"] = userName
        userData["EMAIL"] = email
        userData["TOTAL_SCORE"] = 0


        val documentReference: DocumentReference =
            firestore.collection("USERS")
                .document(userUID)

        val batch = firestore.batch()

        batch.set(documentReference, userData)

        val countDoc = firestore.collection("USERS")
            .document("TOTAL_USERS")

        batch.update(countDoc, "COUNT", FieldValue.increment(1))

        batch.commit().addOnSuccessListener {
            _updateUserInfo.value = Resource.Success(true)

        }.addOnFailureListener {
            _updateUserInfo.value = Resource.Error(it.message.toString())
        }


    }

    fun getUserData() {

        _myPerformance.value = Resource.Loading()

        var user: User

        firestore.collection("USERS")
            .document(firebaseAuth.currentUser?.uid.toString())
            .get()
            .addOnSuccessListener {

                viewModelScope.launch {

                    user = User(
                        userName = it.getString("NAME").toString(),
                        email = it.getString("EMAIL").toString()
                    )

                    Log.d(TAG, "getUserData: total score ${it.getLong("TOTAL_SCORE")!!.toInt()}")

                    myPerformanceLocal.score = (it.getLong("TOTAL_SCORE")!!.toInt())

                    _myPerformance.emit(Resource.Success(myPerformanceLocal))
                    _user.emit(Resource.Success(user))
                }

            }.addOnFailureListener {
                viewModelScope.launch {
                    _myPerformance.emit(Resource.Error(it.message.toString()))
                    _user.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    // Login

    fun login(email: String, password: String) {

        viewModelScope.launch {
            _login.emit(Resource.Loading())
        }

        if (checkLoginCredentials(email, password)) {

            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    viewModelScope.launch {
                        it.user.let {
                            _login.emit(Resource.Success(it))
                        }
                    }

                }.addOnFailureListener {
                    viewModelScope.launch {
                        _login.emit(Resource.Error(it.message.toString()))
                    }
                }
        } else {
            val registerFailedStates = RegisterValidation.RegisterFailedStates(
                validateEmail(email),
                validatePassword(password)
            )

            viewModelScope.launch {
                _validation.send(registerFailedStates)
            }
        }
    }


    fun getGoogleSignInOptions(): GoogleSignInOptions {

        viewModelScope.launch {
            _signInWithGoogle.emit(Resource.Loading())
        }

        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("117980855343-qiridol0q7fkp9a4sdlsvk2st0i5hc31.apps.googleusercontent.com")
            .requestEmail()
            .build()

    }

    fun updateUI(account: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                it.result.additionalUserInfo?.let { additionalUserInfo ->
                    if (additionalUserInfo.isNewUser) {
                        saveUserInfo(
                            firebaseAuth.currentUser!!.uid,
                            firebaseAuth.currentUser!!.email.toString(),
                            firebaseAuth.currentUser!!.displayName.toString()
                        )
                    }
                }


                viewModelScope.launch {
                    _signInWithGoogle.emit(Resource.Success(account))
                }


            } else {
                viewModelScope.launch {
                    _signInWithGoogle.emit(Resource.Error(it.exception.toString()))
                }

            }
        }
    }
}
