package com.companyglobal.alef_app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.companyglobal.alef_app.core.AppConstants
import com.companyglobal.alef_app.core.Result
import com.companyglobal.alef_app.core.utils.SharedPreferencesManager
import com.companyglobal.alef_app.core.utils.Validators
import com.companyglobal.alef_app.data.model.auth.RequestAuth
import com.companyglobal.alef_app.data.model.auth.RequestGoogle
import com.companyglobal.alef_app.data.remote.auth.AuthDataSource
import com.companyglobal.alef_app.databinding.ActivityLoginBinding
import com.companyglobal.alef_app.domain.auth.AuthRepoImpl
import com.companyglobal.alef_app.presentation.auth.AuthViewModel
import com.companyglobal.alef_app.presentation.auth.AuthViewModelFactory
import com.companyglobal.alef_app.services.GoogleVerify
import com.companyglobal.alef_app.services.auth.RetrofitClientAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityLoginBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private val mViewModel by viewModels<AuthViewModel> {
        AuthViewModelFactory(
            AuthRepoImpl(
                AuthDataSource(RetrofitClientAuth.webServiceAuth)
            )
        )
    }

    private var resultLauncherGoogleSignIn =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleSignInResult(task)
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(
                    this,
                    "Hubo un problema, por favor vuelva a intentarlo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mGoogleSignInClient = GoogleVerify.signInGoogle(this)

        methods()
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        resultLauncherGoogleSignIn.launch(signInIntent)
//        goToInfoUser()
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val googleId = account?.id ?: ""
            val googleFirstName = account?.givenName ?: ""
            val googleLastName = account?.familyName ?: ""
            val googleEmail = account?.email ?: ""
            val googleProfilePicURL = account?.photoUrl.toString()
            val googleIdToken = account?.idToken ?: ""

            if (!googleIdToken.isNullOrEmpty()) {
                val authGoogle = RequestGoogle(googleIdToken)
                mViewModel.authGoogle(authGoogle).observe(this) { result ->
                    when (result) {
                        is Result.Failure -> {
                            Log.d("Google", result.toString())
                            mBinding.progressBar.visibility = View.GONE
                        }
                        is Result.Loading -> {
                            Log.d("Google", result.toString())
                            mBinding.progressBar.visibility = View.VISIBLE
                        }
                        is Result.Success -> {
                            Log.d("LoginActivity", result.data.body().toString())
                            val accessToken = result.data.body()?.accessToken.toString()
                            val uid = result.data.body()?.user?.uid.toString()

                            if (accessToken.isNotEmpty() && accessToken != "null") {
                                SharedPreferencesManager.setStringValue(
                                    AppConstants.USER_TOKEN,
                                    accessToken
                                )

                                if (uid.isNotEmpty() && uid != "null") {
                                    SharedPreferencesManager.setStringValue(
                                        AppConstants.USER_ID_GOOGLE,
                                        uid
                                    )
                                    SharedPreferencesManager.setStringValue(
                                        AppConstants.USER_PICTURE_PROFILE,
                                        googleProfilePicURL
                                    )

                                    goToInfoUser()
                                }
                            } else {
                                mBinding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    this,
                                    "Usuario o contraseña incorrecta",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                }
            }

        } catch (e: ApiException) {
            Log.w("Error", "Google sign in failed", e)
        }
    }

    private fun goToInfoUser() {
        val intent = Intent(this@LoginActivity, AvatarActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun methods() {
        with(mBinding) {
            tvForgotPassword.setOnClickListener {
                val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }

            btnLogin.setOnClickListener { validUser() }
            btnGoogle.setOnClickListener { signIn() }
            tvTitleCreateAccount.setOnClickListener { goToRegister() }
            tvCreateAccount.setOnClickListener { goToRegister() }
            btnPreview.setOnClickListener { goToHome(false) }

            etPassword.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
                var handled = false
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    validUser()
                    handled = true
                }
                handled
            }
        }
    }

    private fun validUser() {
        val email: String = mBinding.etEmail.text.toString().trim()
        val password: String = mBinding.etPassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            if (Validators.isValidEmail(email) && Validators.isValidPassword(password)) {
                authUser(email, password)
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Requiere un correo y contraseña mayor a 5 caracteres - validos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this@LoginActivity,
                "Credenciales incorrectas, por favor vuela a intentarlo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun authUser(email: String, password: String) {
        val requestLogin = RequestAuth(email, password)
        mViewModel.signIn(requestLogin).observe(this) { result ->
            when (result) {
                is Result.Failure -> {
                    mBinding.progressBar.visibility = View.GONE
                    Log.d("Auth", result.toString())
                }
                is Result.Loading -> {
                    Log.d("Auth", result.toString())
                    mBinding.progressBar.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    val accessToken = result.data.body()?.accessToken.toString()

                    if (accessToken.isNotEmpty() && accessToken != "null") {
                        goToHome(true, accessToken)
                    } else {
                        mBinding.progressBar.visibility = View.GONE
                        Toast.makeText(this, "Usuario o contraseña incorrecta", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    private fun goToHome(isLoginUser: Boolean, accessToken: String = "") {
        val intent = Intent(this@LoginActivity, HomeActivity::class.java)
        intent.putExtra(AppConstants.IS_LOGIN_USER, isLoginUser)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()

        if (isLoginUser) {
            if (accessToken.isNotEmpty()) {
                SharedPreferencesManager.setStringValue(AppConstants.USER_TOKEN, accessToken)
                finish()
            }
        }
    }

    private fun goToRegister() {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }
}