package com.en1y.alias.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val welcomed = prefs.getBoolean("welcomed", false)

        enableEdgeToEdge()

        if (!welcomed) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        } else {
            println("You have been welcomed")
        }

    }
}