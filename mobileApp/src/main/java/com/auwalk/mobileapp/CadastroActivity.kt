package com.auwalk.mobileapp

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CadastroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
            // Acha o TextView no layout
            val tv = findViewById<TextView>(R.id.textoComp7)

            // Cria o texto estilizado
            val texto = "Já tem conta? Faça login!"
            val spannable = SpannableString(texto)
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                13, 25,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // Cor da palavra "mundo" usando cor do colors.xml
            val cor = ContextCompat.getColor(this, R.color.fonte_destaque_02)
            spannable.setSpan(
                ForegroundColorSpan(cor),
                13,
                25,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Aplica no TextView
            tv.text = spannable

    }
}
