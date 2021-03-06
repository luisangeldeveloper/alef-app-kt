package com.alefglobalintegralproductivityconsulting.alef_app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.alefglobalintegralproductivityconsulting.alef_app.core.AppConstants
import com.alefglobalintegralproductivityconsulting.alef_app.core.utils.SharedPreferencesManager
import com.alefglobalintegralproductivityconsulting.alef_app.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivitySplashScreenBinding

    private var isClosedPreview = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        init()
    }

    private fun init() {
        val args = intent.extras
        if (args != null) {
            isClosedPreview = args.getBoolean(AppConstants.IS_CLOSED_PREVIEW_TIME)
        }

        if (isClosedPreview) {
            delayTime(300L)
        } else {
            delayTime(1000L)
        }
    }

    private fun delayTime(delayMillis: Long) {
        if (SharedPreferencesManager.getStringValue(AppConstants.USER_ID_GOOGLE) != "" &&
            SharedPreferencesManager.getStringValue(AppConstants.USER_TOKEN) != ""
        ) {
            // TODO: Caso 1 - El usuario no ha terminado de llenar el formulario
            Handler(Looper.getMainLooper()).postDelayed({
                goToInfoUser()
            }, delayMillis)
        } else if (SharedPreferencesManager.getStringValue(AppConstants.USER_TOKEN) != "") {
            // TODO: Caso 2 - El usuario ha iniciado sesión
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra(AppConstants.IS_LOGIN_USER, true)
                startActivity(intent)
                finish()
            }, delayMillis)
        } else {
            // TODO: Caso 3 - El usuario aún no inicio sesión
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }, delayMillis)
        }
    }

    private fun goToInfoUser() {
        val intent = Intent(this, AvatarActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}