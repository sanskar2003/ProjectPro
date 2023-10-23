package com.sanskar.projectmanagement.activities

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sanskar.projectmanagement.R

class SpalshActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spalsh)

        auth = Firebase.auth

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler().postDelayed({
            if(auth.currentUser!=null){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            else {
                startActivity(Intent(this, introActivity::class.java),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            }
            Handler().postDelayed({finish()}, 1000)
        }, 3000)
    }
}