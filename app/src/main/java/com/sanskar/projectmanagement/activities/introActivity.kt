package com.sanskar.projectmanagement.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sanskar.projectmanagement.R

class introActivity : BaseActivity() {
    private lateinit var sign_up_tv: TextView
    private lateinit var sign_in_tv: TextView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        sign_up_tv = findViewById(R.id.signUp)
        sign_in_tv = findViewById(R.id.signIn)

        sign_up_tv.setOnClickListener {
            val intent = Intent(this, signUp::class.java)
            startActivity(intent)
        }

        sign_in_tv.setOnClickListener {
            val intent = Intent(this, signIn::class.java)
            startActivity(intent)
        }



    }
}