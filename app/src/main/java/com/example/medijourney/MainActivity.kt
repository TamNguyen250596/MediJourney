package com.example.medijourney

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.activity.ComponentActivity
import com.example.medijourney.modules.SignInActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        setContentView(R.layout.activity_main)
        Handler().postDelayed({
            startActivity(Intent(this@MainActivity, SignInActivity::class.java))
            finish()
        }, 5000)
    }
}