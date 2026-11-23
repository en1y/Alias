package com.en1y.alias.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityOptionsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val welcomed = prefs.getBoolean("welcomed", false)

        enableEdgeToEdge()

        switchTo(if (welcomed) GameActivity::class.java else WelcomeActivity::class.java)
    }
}

fun Activity.switchTo(target: Class<out Activity>) {
    startActivity(Intent(this, target), ActivityOptionsCompat.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
    finish()
}
