package com.sanskar.projectmanagement.activities

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sanskar.projectmanagement.R
import com.sanskar.projectmanagement.firebase.FirestoreClass
import com.sanskar.projectmanagement.models.User

class signIn : BaseActivity() {
    private lateinit var back_btn: LinearLayout
    private lateinit var signinToSignup: TextView
    private lateinit var signInbtn: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var si_email: EditText
    private lateinit var si_password: EditText
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        back_btn = findViewById(R.id.backBtnLayout)
        signinToSignup = findViewById(R.id.signintosignup)
        signInbtn = findViewById(R.id.signInBtn)
        si_email = findViewById(R.id.si_email_input)
        si_password = findViewById(R.id.si_password_input)
        auth = Firebase.auth

        back_btn.setOnClickListener {
            finish()
        }

        signinToSignup.setOnClickListener {
            val intent = Intent(this, signUp::class.java)
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            Handler().postDelayed({ finish()}, 1000)
        }

        signInbtn.setOnClickListener {
            signInUser()
        }
    }

    private fun signInUser() {
        val email: String = si_email.text.toString().trim{ it <= ' ' }
        val password: String = si_password.text.toString().trim{ it <= ' ' }

        if(validateForm(email, password)){
            showProgressDialog("Please Wait")
            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(email, password)
                .addOnCompleteListener{ task ->
                    hideProgressDialog()
                    if(task.isSuccessful){
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        FirestoreClass().loadUserData(this)
                    }
                    else{
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when{
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter a email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }
    }

    fun userSignInSuccess(user: User?) {
        Toast.makeText(this, "You have successfully signed in!",
            Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        Handler().postDelayed({ finish()}, 1000)
    }
}