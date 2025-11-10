package com.auwalk.mobileapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- COR DA STATUS BAR ---
        window.statusBarColor = ContextCompat.getColor(this, R.color.principal)

        // --- COR DOS ÍCONES ---
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false
    }
}
