package com.en1y.alias.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import com.en1y.alias.ui.theme.AliasTheme

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
            startActivity(Intent(this, GameActivity::class.java))
            finish()
            return
        }

    }
}